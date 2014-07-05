package cluster

import akka.actor.{ Actor, ActorRef, Address }
import akka.actor.{ Props, Deploy, Terminated }
import akka.cluster.{ Cluster, ClusterEvent }
import scala.util.Random
import akka.actor.SupervisorStrategy.{ stoppingStrategy }
import akka.remote.RemoteScope
import scala.concurrent.duration._
import akka.actor.ReceiveTimeout

object ClusterReceptionist {
  private case class Job(client: ActorRef, url: String)
  case class Get(url: String)
  case class Result(url: String, links: Set[String])
  case class Failed(url: String, reason: String)
}

class ClusterReceptionist extends Actor {

  import ClusterReceptionist._
  import ClusterEvent.{ MemberUp, MemberRemoved }

  val cluster = Cluster(context.system)

  cluster.subscribe(self, classOf[MemberUp])
  cluster.subscribe(self, classOf[MemberRemoved])

  override def postStop(): Unit = cluster.unsubscribe(self)

  val randoGen = new Random
  def pick[A](coll: IndexedSeq[A]): A = coll(randoGen.nextInt(coll.size))

  def receive = awaitingMembers

  val awaitingMembers: Receive = {
    case Get(url) => sender ! Failed(url, "no nodes available")

    case current: ClusterEvent.CurrentClusterState =>
      val notMe = current.members.toVector map (_.address) filter (_ != cluster.selfAddress)
      if (notMe.nonEmpty) context.become(active(notMe))

    case MemberUp(member) if (member.address != cluster.selfAddress) =>
      context.become(active(Vector(member.address)))
  }

  def active(addresses: Vector[Address]): Receive = {
    
    case Get(url) if (context.children.size < addresses.size) =>
      val client = sender
      val address = pick(addresses)
      context.actorOf(Props(new Customer(client, url, address)))

    case Get(url) => sender ! Failed(url, "too many parallel queries")
    
    case MemberUp(member) if member.address != cluster.selfAddress =>
      context.become(active(addresses :+ member.address))

    case MemberRemoved(member, _) =>
      val next = addresses filterNot (_ == member.address)
      if (next.isEmpty) context.become(awaitingMembers)
      else context.become(active(next))
  }
}

class Customer(client: ActorRef, url: String, node: Address) extends Actor {
  implicit val s = context.parent

  import ClusterReceptionist.Failed

  override val supervisorStrategy = stoppingStrategy
  val props = Props[simple.Controller].withDeploy(Deploy(scope = RemoteScope(node)))
  val controller = context.actorOf(props, "controller")
  context.watch(controller)

  context.setReceiveTimeout(5.seconds)
  controller ! simple.Controller.Check(url, 2)

  def receive = onResponse andThen (_ => context.stop(self))

  val onResponse: Receive = {

    case ReceiveTimeout =>
      context.unwatch(controller)
      client ! Failed(url, "controller timed out")

    case Terminated(_) =>
      client ! Failed(url, "controller died")

    case simple.Controller.Result(links) =>
      context.unwatch(controller)
      client ! ClusterReceptionist.Result(url, links)

  }

}
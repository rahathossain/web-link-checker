package cluster

import akka.actor.{ Actor, Props, ReceiveTimeout }
import akka.cluster.{ Cluster, ClusterEvent }
import scala.concurrent.duration._

class ClusterMain extends Actor {

  import ClusterReceptionist._
  import ClusterEvent.{ MemberUp, MemberRemoved }

  val cluster = Cluster(context.system)
  cluster.subscribe(self, classOf[MemberUp])
  cluster.subscribe(self, classOf[MemberRemoved])
  cluster.join(cluster.selfAddress)

  val cReceptionist = context.actorOf(Props[ClusterReceptionist], "cReceptionist")
  context.watch(cReceptionist)

  def getLater(d: FiniteDuration, url: String): Unit = {
    import context.dispatcher
    context.system.scheduler.scheduleOnce(d, cReceptionist, Get(url))
  }

  getLater(Duration.Zero, "http://www.google.com")

  def receive = {
    
    case ClusterEvent.MemberUp(member) =>
      if (member.address != cluster.selfAddress) {
        getLater(1.seconds, "http://www.google.com")
        getLater(2.seconds, "http://www.google.com/1")
        getLater(2.seconds, "http://www.google.com/2")
        getLater(3.seconds, "http://www.google.com/3")
        getLater(4.seconds, "http://www.google.com/4")
        context.setReceiveTimeout(3.seconds)
      }
      
    case Result(url, set) =>
      println(set.toVector.sorted.mkString(s"Results for '$url':\n", "\n", "\n"))
      
    case Failed(url, reason) =>
      println(s"Failed to fetch '$url': $reason\n")
      
    case ReceiveTimeout =>
      cluster.leave(cluster.selfAddress)
      
    case ClusterEvent.MemberRemoved(m, _) =>
      context.stop(self)
  }

}
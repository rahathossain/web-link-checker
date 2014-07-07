package simple

import akka.actor.{ Actor, ActorRef, Props, ReceiveTimeout, Terminated, ActorLogging, OneForOneStrategy }
import akka.actor.SupervisorStrategy.{ Restart }
import scala.concurrent.duration._

object Controller {
  case class Check(url: String, depth: Int)
  case class Result(links: Set[String])  
}

class Controller extends Actor with ActorLogging {   
  import Controller._
  import Config.ControllerTimeout
  
  var cache = Set.empty[String]
  context.setReceiveTimeout(ControllerTimeout.seconds)

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5) {
    case _: Exception => Restart
  }

  def getterProps(url: String, depth: Int) = Props(new Getter(url, depth))

  def receive = {
    case Check(url, depth) =>
      log.debug("\n\n\t**** {} checking {}\n", depth, url)
      if (!cache(url) && depth > 0)
        context.watch(context.actorOf(getterProps(url, depth - 1)))
      cache += url
    case Terminated(_) =>
      if (context.children.isEmpty)
        context.parent ! Result(cache)
    case ReceiveTimeout =>
      context.children foreach context.stop
  }
}
package simple

import akka.actor.{Actor, Props, ReceiveTimeout}
import scala.concurrent.duration._


class Main extends Actor {
  
  import Receptionist._
  
  val receptionist = context.actorOf(Props[Receptionist], "receptionist")
  
  receptionist ! Get("http://www.google.com")
  
  context.setReceiveTimeout(30.seconds)
  
  
  def receive = {
    case Result(url, set) =>
      println(set.toVector.sorted.mkString(s"Result for '$url'\n", "\n", "\n" ))
    case Failed(url) => 
      println(s"Failed to fetch '$url'\n")
    case ReceiveTimeout =>
      context.stop(self)
  }
  
  
  override def postStop(): Unit = {
    AsyncWebClient.shutdown()
  }

}
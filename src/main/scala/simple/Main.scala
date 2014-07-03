package simple

import akka.actor.{Actor, Props, ReceiveTimeout}
import scala.concurrent.duration._

class Main extends Actor {
  import Receptionist._
  
  val TIMEOUT = 10
  val testURL = "http://www.google.com"
  val receptionist = context.actorOf(Props[Receptionist], "receptionist")
  
  receptionist ! Get(testURL)
  
  context.setReceiveTimeout(TIMEOUT.seconds)
  
  def receive = {
    case Result(url, set) => 
      println(set.toVector.sorted.mkString(s"Result for '$url'\n", "\n", "\n" ))
      context.stop(self)
    case Failed(url) => println(s"Failed to fetch '$url'\n")
    case ReceiveTimeout => context.stop(self)
  }
  
  override def postStop(): Unit = AsyncWebClient.shutdown()
}
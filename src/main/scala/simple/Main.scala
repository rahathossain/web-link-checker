package simple

import akka.actor.{ Actor, Props, ReceiveTimeout }
import scala.concurrent.duration._

class Main extends Actor {
  import Receptionist._
  import Config.{MainTimeout, testURL}
  
  val receptionist = context.actorOf(Props[Receptionist], "receptionist")
  context.watch(receptionist)

  receptionist ! Get(testURL)
  //receptionist ! Get(testURL+"/1")
  //receptionist ! Get(testURL+"/2")
  //receptionist ! Get(testURL+"/3")
  //receptionist ! Get(testURL+"/4")
  //receptionist ! Get(testURL+"/5")

  context.setReceiveTimeout(MainTimeout.seconds)

  def receive = {
    case Result(url, set) =>
      println(set.toVector.sorted.mkString(s"Result for '$url'\n", "\n", "\n"))
    case Failed(url) => println(s"Failed to fetch '$url'\n")
    case ReceiveTimeout => context.stop(self)
  }

  override def postStop(): Unit = AsyncWebClient.shutdown()

}
package simple

import akka.actor.{Actor, Props, ReceiveTimeout}
import scala.concurrent.duration._


class Main extends Actor {
  
  import Receptionist._
  
  val receptionist = context.actorOf(Props[Receptionist], "receptionist")
  
  //receptionist ! Get("http://www.google.com")
  receptionist ! Get("http://www.seek.com.au/jobs-in-information-communication-technology/in-sydney/#dateRange=1&workType=242&industry=6281&occupation=&graduateSearch=false&salaryFrom=100000&salaryTo=999999&salaryType=annual&advertiserID=&advertiserGroup=&keywords=&page=1&displaySuburb=&seoSuburb=&isAreaUnspecified=false&location=1000&area=&nation=&sortMode=ListedDate&searchFrom=advanced&searchType=")
  //-Dakka.loglevel=DEBUG -Dakka.actor.debug.receive=on
  
  
  context.setReceiveTimeout(10.seconds)
  
  
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
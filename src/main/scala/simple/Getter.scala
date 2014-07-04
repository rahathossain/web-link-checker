package simple

import akka.actor.{ Actor, ActorRef, ActorLogging, Status }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executor
import akka.pattern.pipe

object Getter {
  case object Done
  case object Abort
}

class Getter(url: String, depth: Int) extends Actor with ActorLogging {  
  import Getter._
  implicit val executor = context.dispatcher.asInstanceOf[Executor with ExecutionContext]
  def client: WebClient = AsyncWebClient
  
  val req_time = System.currentTimeMillis()

  client get url pipeTo self

  def receive = {    
    case body: String =>
      val res_time = System.currentTimeMillis()
      val diff  = res_time - req_time
      log.debug(s"\n\n\t**** $url took $diff miliseconds to response\n")
      log.debug(s"\n\n\t**** path = {} \n ", self.path) 
      for (link <- findLinks(body))
        context.parent ! Controller.Check(link, depth)
      context.stop(self)
    case _: Status.Failure => context.stop(self)
  }

  val A_TAG = "(?i)<a ([^>]+)>.+?</a>".r
  val HREF_ATTR = """\s*(?i)href\s*=\s*(?:"([^"]*)"|'([^']*)'|([^'">\s]+))\s*""".r

  def findLinks(body: String): Iterator[String] = {
    def check(u: String) = if ((u indexOf ('.', 0)) == -1) (url take (url indexOf ('/', 8))) + u else u
    for {
      anchor <- A_TAG.findAllMatchIn(body)
      HREF_ATTR(dquot, quot, bare) <- anchor.subgroups
    } yield if (dquot != null) check(dquot)
    else if (quot != null) check(quot)
    else check(bare)
  }
}
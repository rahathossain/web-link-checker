package simple

import java.util.concurrent.Executor
import scala.concurrent.{Future}

import akka.actor.Props
import akka.testkit.{TestKit, ImplicitSender}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll}
import akka.actor.{Actor, ActorRef, ActorSystem}

class StepParent(child: Props, fwd: ActorRef) extends Actor {
  context.actorOf(child, "child")
  def receive = {
    case msg => fwd.tell(msg, sender)
  }
}

object GetterSpec {
	
  val firstLink = "http://www.rhossain.com/1"
  
  val bodies = Map(
    firstLink ->
      """<html>
        |  <head><title>Page 1</title></head>
        |  <body>
        |    <h1>A Link</h1>
        |   <a href="http://www.rhossain.com/2">click here</a>
        |  </body>
        |</html>""".stripMargin)
        

  val links = Map(
    firstLink -> Seq("http://www.rhossain.com/2"))
    
  object FakeWebClient extends WebClient  {
    def get(url: String)(implicit exec: Executor): Future[String] =  
      bodies get url match {
      	case None		=> Future.failed(BadStatus(404)) 
      	case Some(body) => Future.successful(body)      
      }    
  }
  
  def fakeGetter(url: String, depth: Int): Props =
    Props(new Getter(url, depth) {
      override def client = FakeWebClient
    })  
}



class GetterSpec extends TestKit(ActorSystem("GetterSpec")) 
	with WordSpecLike with BeforeAndAfterAll with ImplicitSender {
  
  import GetterSpec._
  
  override def afterAll(): Unit = {
    system.shutdown()
  }
  
  "A Getter" must {
    
    
    "return the right body" in {
      val getter = system.actorOf(Props(new StepParent(fakeGetter(firstLink, 2), testActor)), "rightBody")
      for (link <- links(firstLink))
        expectMsg(Controller.Check(link, 2))
      expectMsg(Getter.Done)  
    }
    
    "properly finish in case of errors" in {
      val getter = system.actorOf(Props(new StepParent(fakeGetter("unknown", 2), testActor)), "wrongLink")
      expectMsg(Getter.Done)
    }
    
  } 
  
}
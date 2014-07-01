package simple

import akka.testkit.{TestKit, ImplicitSender}
import akka.actor.{Actor, Props, ActorSystem}
import scala.concurrent.duration._
import org.scalatest.{WordSpecLike, BeforeAndAfterAll}

object ReceptionistSpec {
  
	class FakeConroller extends Actor {
	  import context.dispatcher
	  
	  def receive = {
	    case Controller.Check(url, depth) =>
	      context.system.scheduler.scheduleOnce(1.second, sender, Controller.Result(Set(url)))
	  }
	}
	
	def fakeReceptionist: Props = 
	  Props(new Receptionist{
	    override def controllerProps = Props[FakeConroller]
	  })
}


class ReceptionistSpec extends TestKit(ActorSystem("ReceptionistSpec"))
	with WordSpecLike with BeforeAndAfterAll with ImplicitSender{
  
	import ReceptionistSpec._
	import Receptionist._
	
	override def afterAll(): Unit = {
	  system.shutdown()
	}
	
	"A Receptionist" must {
	  
	  "reply with a result" in {
	    val receptionist = system.actorOf(fakeReceptionist, "sendResult")
	    receptionist ! Get("myUrl")
	    expectMsg(Result("myUrl", Set("myUrl")))
	  }
	  
	  "reject request flood" in {
	    val receptionist = system.actorOf(fakeReceptionist, "rejectFlood")
	    for(i <- 1 to 5) receptionist ! Get(s"myUrl$i")
	    expectMsg(Failed("myUrl5")) 
	    for(i <- 1 to 4) expectMsg(Result(s"myUrl$i", Set(s"myUrl$i")))
	  }
	  
	}
	
}
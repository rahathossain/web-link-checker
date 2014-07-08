package cluster


//import scala.concurrent.duration._
//import scala.util.Random
import com.typesafe.config.{ConfigFactory, Config}
//import akka.actor.ActorSystem
import akka.actor.Props
import akka.kernel.Bootable



class BootClusterMain extends Bootable {
  
    val str = 
    """akka {
		  loggers = ["akka.event.slf4j.Slf4jLogger"]  
		  loglevel = DEBUG
		  actor {
		  	provider = akka.cluster.ClusterActorRefProvider      
		    debug {        
		      receive = on
		    }
		  }
		  cluster {
		  	min-nr-of-members=2
		  	auto-down = on
		  } 
		}"""    
      
    val config = ConfigFactory.parseString(str)
    val system = akka.actor.ActorSystem("Main", config)
    
 
  def startup = {      
    val clusterMain = system.actorOf(Props[ClusterMain], "app")
    println("Started ClusterMain - waiting for messages\n\n\n")  
  }
 
  def shutdown = system.shutdown()
  
}
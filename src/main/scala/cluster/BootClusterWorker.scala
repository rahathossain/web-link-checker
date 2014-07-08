package cluster

import com.typesafe.config.{ConfigFactory, Config}
import akka.actor.Props
import akka.kernel.Bootable


class BootClusterWorker extends Bootable {

        val str = """    
		akka {
		  loggers = ["akka.event.slf4j.Slf4jLogger"]  
		  loglevel = DEBUG
		  actor {
		  	provider = akka.cluster.ClusterActorRefProvider      
		    debug {
		      receive = on
		    }		    
		  }		  
          remote.netty.tcp.port = 0
          
		  cluster {
		  	min-nr-of-members=2
		  	auto-down = on
		  } 
		}
		"""
    
    val config = ConfigFactory.parseString(str)
    val system = akka.actor.ActorSystem("Main", config)
    
  def startup = {      
    val clusterWorker = system.actorOf(Props[ClusterWorker], "app")
    
    println("Started LookupSystem\n\n\n")    
  }
 
  def shutdown = system.shutdown() 
   
}

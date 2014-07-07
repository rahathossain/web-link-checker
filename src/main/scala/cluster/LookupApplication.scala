package cluster


import scala.concurrent.duration._
import scala.util.Random
import com.typesafe.config.{ConfigFactory, Config}
import akka.actor.ActorSystem
import akka.actor.Props

/*
object ClusterMain {
	val system = akka.actor.ActorSystem("Main")
	    val clusterMain = system.actorOf(Props[ClusterMain], "ClusterMain")
  }
}
object ClusterWorker {  
    val system = akka.actor.ActorSystem("Main")
    val clusterWorker = system.actorOf(Props[ClusterMain], "ClusterWorker")
  }
}
*/

object LookupApplication {
  def main(args: Array[String]): Unit = {
    if (args.head == "Worker") startClusterWorker()
    
    if (args.head == "Main") startClusterMain()

  }

  def startClusterMain(): Unit = {
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
    
    val clusterMain = system.actorOf(Props[ClusterMain], "clusterMain")

    println("Started ClusterMain - waiting for messages\n\n\n")
  }

  def startClusterWorker(): Unit = {
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
		  
          remote.netty.tcp.port=0
          
		  cluster {
		  	min-nr-of-members=2
		  	auto-down = on
		  } 
		}     
		"""
    
    val config = ConfigFactory.parseString(str)

    val system = akka.actor.ActorSystem("Main", config)
    
    val clusterWorker = system.actorOf(Props[ClusterWorker], "clusterWorker")
    
    println("Started LookupSystem\n\n\n")    
  }
}


/*
object ClusterMain {
  def main(args: Array[String]) {
	    implicit val system = akka.actor.ActorSystem("Main")
	    val clusterMain = system.actorOf(Props[ClusterMain], "ClusterMain")
  }
}


object ClusterWorker1 {
  def main(args: Array[String]) = {
    implicit val system = akka.actor.ActorSystem("Main")
    val clusterWorker = system.actorOf(Props[ClusterMain], "ClusterWorker")
  }
}
*/

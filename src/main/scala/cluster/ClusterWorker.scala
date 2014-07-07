package cluster

import akka.actor.{Actor, ActorLogging, RootActorPath, Identify, ActorIdentity, Terminated, Props}
import akka.cluster.{Cluster, ClusterEvent}
import simple.AsyncWebClient

class ClusterWorker extends Actor with ActorLogging {
  
    import ClusterEvent.{MemberUp, MemberRemoved}
	
	val cluster = Cluster(context.system)
	cluster.subscribe(self, classOf[MemberUp])
	cluster.subscribe(self, classOf[MemberRemoved])
	val main = cluster.selfAddress.copy(port = Some(2552))
	cluster.join(main)
	
	def receive = {
	  case MemberUp(member) => 
	    if (member.address == main)
	      context.actorSelection(RootActorPath(main) / "user" / "app" / "cReceptionist") ! Identify("42")
	      
	  case ActorIdentity("42", None) => context.stop(self)
	  
	  case ActorIdentity("42", Some(ref)) => 
	    log.info(s"cReceptionist is at $ref")
	    context.watch(ref)
	    
	  case Terminated(_) => context.stop(self)
	  
	  case MemberRemoved(m, _) => if(m.address == main) context.stop(self)
	}
	
  override def postStop(): Unit = AsyncWebClient.shutdown()
  	
	
	
}
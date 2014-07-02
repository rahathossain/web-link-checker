package simple

import akka.actor.{Actor, Props, ActorRef, Terminated}
import akka.actor.SupervisorStrategy.{ stoppingStrategy }

object Receptionist {
  private case class Job(client: ActorRef, url: String)
  case class Get(url: String)
  case class Result(url: String, links: Set[String])
  case class Failed(url: String)
  val DEPTH = 2
}

class Receptionist extends Actor {
  import Receptionist._ 
   
  override def supervisorStrategy = stoppingStrategy
  
  def controllerProps: Props = Props[Controller]
  var reqNo = 0
  def receive = waiting
  
  val waiting: Receive = {
    case Get(url) => context.become(runNext(Vector(Job(sender, url))))
  }
  
  def running(queue: Vector[Job]): Receive = {
    case Controller.Result(links) =>
      val job = queue.head
      job.client ! Result(job.url, links)
      context.stop(context.unwatch(sender))
      context.become(runNext(queue.tail))
    case Terminated(_) =>
      val job = queue.head
      job.client ! Failed(job.url)
      context.become(runNext(queue.tail))
    case Get(url) => context.become(enqueueJob(queue, Job(sender, url)))
  }
  
  def runNext(queue: Vector[Job]): Receive = {
    reqNo += 1
    if(queue.isEmpty) waiting
    else{
      val controller = context.actorOf(controllerProps, s"c$reqNo")
      context.watch(controller) 
      controller ! Controller.Check(queue.head.url, DEPTH)
      running(queue)
    }
  }
  
  def enqueueJob(queue: Vector[Job], job: Job) : Receive = {
    if(queue.size > 3) {
      sender ! Failed(job.url)
      running(queue)
    } else running(queue :+ job)
  }  
}

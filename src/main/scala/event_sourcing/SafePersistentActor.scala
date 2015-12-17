package event_sourcing

import akka.actor.{PoisonPill, Props, ActorSystem, ActorLogging}
import akka.persistence.PersistentActor

case object Shutdown

class SafePersistentActor extends PersistentActor with ActorLogging {
  override def receiveRecover: Receive = {
    case _=>
  }

  override def receiveCommand: Receive = {
    case c: String =>
      log.info(c)
      persist(s"handle-$c"){log.info}
    case Shutdown =>
      context.stop(self)
  }

  override def persistenceId: String = "safe-actor"
}

object SafePersistentActor extends App {
  val system = ActorSystem("safeSystem")
  val persistentActor = system.actorOf(Props[SafePersistentActor])
  persistentActor ! "a"
  persistentActor ! "b"
  //persistentActor ! PoisonPill
  persistentActor ! Shutdown


  Thread.sleep(2000)
  system.terminate()
}
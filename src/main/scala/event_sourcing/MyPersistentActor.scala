package event_sourcing

import akka.actor.{ActorLogging, Props, ActorSystem}
import akka.persistence.PersistentActor

class MyPersistentActor extends PersistentActor with ActorLogging {
  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = {
    case c: String =>
      log.info(s"Received $c")
      sender() ! c
      persistAsync(s"evt-$c-1") { e => sender() ! e }
      persistAsync(s"evt-$c-2") { e => sender() ! e }
      deferAsync(s"evt-$c-2") { e => sender() ! e }
  }

  override def persistenceId: String = "my-stable-persistent-actor"
}

object MyPersistentActor extends App {
  val system = ActorSystem("cucu")
  val pa = system.actorOf(Props[MyPersistentActor])
  pa ! "cucu"
  pa ! "rigu"
  Thread.sleep(2000)
  system.terminate()
}
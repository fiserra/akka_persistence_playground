package com.fiser.akka.persistence

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor

class MyPersistentActor extends PersistentActor with ActorLogging {
  override def persistenceId = "stable-persistence-id"

  def receiveRecover: Receive = {
    case _ => // handle recovery here
  }

  override def receiveCommand: Receive = {
    case c: String =>
      sender() ! c
      persistAsync(s"evt-$c-1") { e =>
        log.info("Handling 1 {}", e)
        sender() ! e
      }
      persistAsync(s"evt-$c-2") { e =>
        log.info("Handling 2 {}", e)
        sender() ! e
      }
      defer(s"evt-$c-3") { e =>
        log.info("Deferring {}", e)
        sender() ! e
      }
  }
}

object MyPersistentActor extends App {
  val system = ActorSystem("actorSys")
  val persistentActor = system.actorOf(Props[MyPersistentActor], "myPersistentActor")
  persistentActor ! "a"
  persistentActor ! "b"

  Thread.sleep(1000)
  system.shutdown()
}

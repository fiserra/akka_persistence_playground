package com.fiser.akka.persistence

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor

object PersistentActorFailureExample extends App {

  val system = ActorSystem("example2")
  val persistentActor = system.actorOf(Props(classOf[ExamplePersistentActor]), "persistentActor-2")

  persistentActor ! "a"
  persistentActor ! "print"
  persistentActor ! "boom" // restart and recovery
  persistentActor ! "print"
  persistentActor ! "b"
  persistentActor ! "print"
  persistentActor ! "c"
  persistentActor ! "print"

  Thread.sleep(1000)
  system.shutdown()


  class ExamplePersistentActor extends PersistentActor with ActorLogging {

    override def persistenceId: String = "sample-id-2"

    var received: List[String] = Nil

    override def receiveRecover: Receive = {
      case s: String =>
        log.info("Recovering {}", s)
        received = s :: received
    }

    override def receiveCommand: Receive = {
      case "print" => log.info("received {}", received.reverse)
      case "boom" =>
        log.error("Throwing an exception")
        throw new Exception("boom")
      case s: String =>
        persist(s) {
          evt => received = s :: received
        }
    }
  }
}



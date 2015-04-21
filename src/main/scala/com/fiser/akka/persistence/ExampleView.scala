package com.fiser.akka.persistence

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, PersistentView}

object ViewExample extends App {

  class ExamplePersistentActor extends PersistentActor {
    override def persistenceId: String = "sample-id-4"

    private var count = 1

    override def receiveRecover: Receive = ???

    override def receiveCommand: Receive = {
      case _: String => count += 1
    }
  }

  class ExampleView extends PersistentView with ActorLogging {
    override def persistenceId: String = "some-persistence-id"

    override def viewId: String = "some-persistence-id-view"

    override def receive: Receive = {
      case payload if isPersistent =>
        log.info("Persistent {} received", payload)
      case payload =>
        log.info("{} received", payload)
    }
  }

}

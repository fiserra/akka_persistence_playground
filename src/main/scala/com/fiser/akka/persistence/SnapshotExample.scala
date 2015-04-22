package com.fiser.akka.persistence

import akka.actor.{Props, ActorSystem, ActorLogging}
import akka.persistence.{SnapshotOffer, SaveSnapshotSuccess, SaveSnapshotFailure, PersistentActor}

object SnapshotExample extends App {

  val system = ActorSystem("example")
  val persistentActor = system.actorOf(Props(classOf[ExamplePersistentActor]), "persistentActor-3-scala")

  persistentActor ! "a"
  persistentActor ! "b"
  persistentActor ! "snap"
  persistentActor ! "c"
  persistentActor ! "d"
  persistentActor ! "snap"
  persistentActor ! "print"

  Thread.sleep(1000)
  system.shutdown()

  case class ExampleState(received: List[String] = Nil) {
    def updated(s: String): ExampleState = copy(s :: received)
    override def toString = received.reverse.toString()
  }

  class ExamplePersistentActor extends PersistentActor with ActorLogging {
    private var state = ExampleState()

    override def persistenceId: String = "sample-id-3"

    override def receiveRecover: Receive = {
      case SnapshotOffer(_, s: ExampleState) =>
        log.info("offered state = {}", s)
        state = s
      case evt: String =>
        state = state.updated(evt)
    }

    override def receiveCommand: Receive = {
      case "print" => log.info("Current state {}", state)
      case "snap" =>
        log.info("Saving state {}", state)
        saveSnapshot(state)
      case snapshot@SaveSnapshotSuccess(metadata) => log.info("Successfully saved snapshot {}", snapshot)
      case snapshot@SaveSnapshotFailure(metadata, reason) => log.error("Failed to save snapshot {}", snapshot)
      case s: String =>
        persist(s) {
          evt => state = state.updated(evt)
        }
    }
  }
}



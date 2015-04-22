package com.fiser.akka.persistence

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence._

import scala.concurrent.duration._

object ViewExample extends App {

  val system = ActorSystem("example")

  val persistentActor = system.actorOf(Props(classOf[ExamplePersistentActor]))
  val view = system.actorOf(Props(classOf[ExampleView]))

  import system.dispatcher

  system.scheduler.schedule(Duration.Zero, 2.seconds, persistentActor, "scheduled")
  system.scheduler.schedule(Duration.Zero, 5.seconds, view, "snap")


  class ExamplePersistentActor extends PersistentActor with ActorLogging {
    override def persistenceId: String = "sample-id-4"

    private var count = 1

    override def receiveRecover: Receive = {
      case payload: String =>
        log.info("Recovering persistentActor {}", payload)
        count += 1
    }

    override def receiveCommand: Receive = {
      case payload: String =>
        log.info(s"persistentActor received $payload (nr = $count)")
        persist(s"$payload-$count") {
          evt =>
            log.info("Successfully persisted {}", evt)
            count += 1
        }
    }
  }

  class ExampleView extends PersistentView with ActorLogging {
    private var numReplicated = 0

    override def persistenceId: String = "sample-id-4"

    override def viewId: String = "sample-view-id-4"

    override def receive: Receive = {
      case "snap" =>
        log.info(s"view snapshots $numReplicated")
        saveSnapshot(numReplicated)
      case SnapshotOffer(metadata, snapshot: Int) =>
        numReplicated = snapshot
        log.info(s"view received snapshot offer $snapshot (metadata = $metadata)")
      case payload if isPersistent =>
        numReplicated += 1
        log.info(s"view received persistent $payload (num replicated = $numReplicated)")
      case snapshot@SaveSnapshotSuccess(metadata) => log.info("Successfully saved snapshot {}", snapshot)
      case snapshot@SaveSnapshotFailure(metadata, reason) => log.error("Failed to save snapshot {}", snapshot)
      case payload =>
        log.info("view received not persistent {}", payload)
    }


  }

}

package com.fiser.akka.persistence

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence._


case class Cmd(data: String)

case class Evt(data: String)

case class ExampleState(events: List[String] = Nil) {
  def updated(evt: Evt): ExampleState = copy(evt.data :: events)

  def size: Int = events.length

  override def toString = events.reverse.toString()
}

class ExamplePersistenceActor extends PersistentActor with ActorLogging {
  var state = ExampleState()

  def updateState(evt: Evt): Unit = {
    state = state.updated(evt)
  }

  def numEvents = state.size

  def recoveryCompleted(): Unit = {
    log.info(s"Recovery Completed: RecoveryFinished: {}, Recovery Running: {}", recoveryFinished, recoveryRunning)
  }

  override def receiveRecover: Receive = {
    case RecoveryCompleted => recoveryCompleted()
    case evt: Evt => updateState(evt)
    case SnapshotOffer(_, snapshot: ExampleState) => state = snapshot
  }

  override def receiveCommand: Receive = {
    case Cmd(data) =>
      persist(Evt(s"$data-$numEvents"))(updateState)
      persist(Evt(s"$data-${numEvents + 1}")) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
      }
    case "snap" => saveSnapshot(state)
    case "print" => println(state)
  }

  override def persistenceId: String = "sample-id-1"
}

object ExamplePersistenceActor extends App {
  val system = ActorSystem("actorSys")
  val persistentActor = system.actorOf(Props[ExamplePersistenceActor], "persistenceActor")
  persistentActor ! Cmd("foo")
  persistentActor ! Cmd("baz")
  persistentActor ! Cmd("bar")
  persistentActor ! "snap"
  persistentActor ! Cmd("buzz")
  persistentActor ! "print"

  Thread.sleep(1000)
  system.shutdown()
}

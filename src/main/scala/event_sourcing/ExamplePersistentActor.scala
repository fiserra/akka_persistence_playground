package event_sourcing

import akka.actor.{Props, ActorSystem, ActorLogging}
import akka.persistence._

case class Cmd(data: String)

case class Evt(data: String)

case class ExampleState(events: List[String] = Nil) {
  def size = events.length

  def updated(evt: Evt): ExampleState = copy(evt.data :: events)

  override def toString: String = events.reverse.toString
}

class ExamplePersistentActor extends PersistentActor with ActorLogging {
  override def persistenceId: String = "sample-id-1"

  private var state = ExampleState()

  private def numEvents = state.size

  private def updateState(evt: Evt) {
    state = state.updated(evt)
  }

  override def receiveCommand: Receive = {
    case Cmd(data) ⇒
      persist(Evt(s"$data-$numEvents")) { evt: Evt ⇒ updateState(evt) }
      persist(Evt(s"$data-${numEvents + 1}")) { evt ⇒
        updateState(evt)
        context.system.eventStream.publish(evt)
      }
    case "snap" ⇒ saveSnapshot(state)
    case SaveSnapshotSuccess(metadata) ⇒ log.info(s"Snapshot saved successfully. ${metadata.toString}")
    case SaveSnapshotFailure(metadata, reason) ⇒ log.info(s"Snapshot failed: $reason. ${metadata.toString}")
    case "print" ⇒ log.info(state.toString)
  }

  override def receiveRecover: Receive = {
    case evt: Evt ⇒ updateState(evt)
    case SnapshotOffer(_, snapshot: ExampleState) ⇒
      log.info(s"Setting state to $snapshot")
      state = snapshot
    case RecoveryCompleted ⇒
      log.info("Recovery Completed")
  }
}

object ExamplePersistentActor extends App {
  val actorSystem = ActorSystem("system")
  val persistentActor = actorSystem.actorOf(Props[ExamplePersistentActor])
  persistentActor ! Cmd("cucu")
  persistentActor ! "snap"
  persistentActor ! Cmd("rigu")

  Thread.sleep(3000)

  actorSystem.terminate()
}

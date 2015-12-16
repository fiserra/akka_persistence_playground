package event_sourcing

import akka.persistence.{PersistentActor, SnapshotOffer}

case class Cmd(data: String)

case class Evt(data: String)

case class ExampleState(events: List[String] = Nil) {
  def size = events.length

  def updated(evt: Evt): ExampleState = copy(evt.data :: events)

  override def toString: String = events.reverse.toString
}

class ExamplePersistentActor extends PersistentActor {
  override def persistenceId: String = "sample-id-1"

  private var state = ExampleState()

  private def numEvents = state.size

  private def updateState(evt: Evt) {
    state = state.updated(evt)
  }

  override def receiveCommand: Receive = {
    case Cmd(data) =>
      persist(Evt(s"$data-$numEvents")) { evt: Evt => updateState(evt) }
      persist(Evt(s"$data-${numEvents + 1}")) { evt =>
        updateState(evt)
        context.system.eventStream.publish(evt)
      }
    case "snap" => saveSnapshot(state)
    case "print" => println(state)

  }

  override def receiveRecover: Receive = {
    case evt: Evt ⇒ updateState(evt)
    case SnapshotOffer(_, snapshot: ExampleState) => state = snapshot
  }
}

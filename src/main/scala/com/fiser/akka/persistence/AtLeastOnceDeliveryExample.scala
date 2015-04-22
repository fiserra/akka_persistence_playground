package com.fiser.akka.persistence

import akka.actor._
import akka.persistence.{AtLeastOnceDelivery, PersistentActor}

object AtLeastOnceDeliveryExample extends App {

  val system = ActorSystem("atLeastOnceDeliveryExample")
  val destination = system.actorOf(Props(classOf[MyDestination]), "myDestination-4")
  val persistentActor = system.actorOf(Props(classOf[MyPersistentActor], destination.path), "persistentActor-4")

  persistentActor ! "a"
  persistentActor ! "b"
  persistentActor ! "c"

  Thread.sleep(1000)
  system.shutdown()

  case class Msg(deliveryId: Long, s: String)

  case class Confirm(deliveryId: Long)

  sealed trait Evt

  case class MsgSent(s: String) extends Evt

  case class MsgConfirmed(deliveryId: Long) extends Evt

  class MyPersistentActor(destination: ActorPath) extends PersistentActor with AtLeastOnceDelivery with ActorLogging {
    override def receiveRecover: Receive = {
      case evt: Evt => updateState(evt)
    }

    override def receiveCommand: Receive = {
      case s: String =>
        log.info("Received {}", s)
        persist(MsgSent(s))(updateState)
      case c@Confirm(deliveryId) =>
        log.info("Received {} ", c)
        log.info("Persisting {}", MsgConfirmed(deliveryId))
        persist(MsgConfirmed(deliveryId))(updateState)
    }

    private def updateState(event: Evt): Unit = event match {
      case msg@MsgSent(s) =>
        log.info("Deliver {} to {} ", msg, destination)
        deliver(destination, deliveryId => Msg(deliveryId, s))
      case MsgConfirmed(deliveryId) =>
        log.info("Delivery {} confirmed ", deliveryId)
        confirmDelivery(deliveryId)
    }
  }

  class MyDestination extends Actor with ActorLogging {
    def receive = {
      case msg@Msg(deliveryId, s) =>
        log.info("Confirming receiving of {} ", msg)
        sender() ! Confirm(deliveryId)
    }
  }
}

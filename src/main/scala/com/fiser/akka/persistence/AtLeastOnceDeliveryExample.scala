package com.fiser.akka.persistence

import akka.actor.{Actor, ActorPath}
import akka.persistence.{AtLeastOnceDelivery, PersistentActor}

object AtLeastOnceDeliveryExample extends App {

  case class Msg(deliveryId: Long, s: String)

  case class Confirm(deliveryId: Long)

  sealed trait Evt

  case class MsgSent(s: String) extends Evt

  case class MsgConfirmed(deliveryId: Long) extends Evt

  class MyPersistentActor(destination: ActorPath) extends PersistentActor with AtLeastOnceDelivery {
    override def receiveRecover: Receive = ???

    override def receiveCommand: Receive = {
      case s: String => persist(MsgSent(s))(updateState)
      case Confirm(deliveryId) => persist(MsgConfirmed(deliveryId))(updateState)
    }

    private def updateState(event: Evt): Unit = {
      case MsgSent(s) =>
        deliver(destination, deliveryId => Msg(deliveryId, s))
      case MsgConfirmed(deliveryId) => confirmDelivery(deliveryId)

    }
  }

  class MyDestination extends Actor {
    def receive = {
      case Msg(deliveryId, s) =>
        // ...
        sender() ! Confirm(deliveryId)
    }
  }

}

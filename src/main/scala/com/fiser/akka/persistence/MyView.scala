package com.fiser.akka.persistence

import akka.actor.ActorLogging
import akka.persistence.PersistentView

class MyView extends PersistentView with ActorLogging {
  override def persistenceId: String = "some-persistence-id"
  override def viewId: String = "some-persistence-id-view"

  override def receive: Receive = {
    case payload if isPersistent =>
      log.info("Persistent {} received", payload)
    case payload =>
      log.info("{} received", payload)
  }
}

package vn.flinters

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import vn.flinters.testMQ.MessageQueue._

class MyPriorityMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(
    // Define the priority generator logic
    PriorityGenerator {
      // Assign higher priority to messages based on specific criteria
      case DownloadLink => 0
      case ProcessDownloadedFiles => 1
      case SomeLowPriorityMessage => 2
      // Assign a default priority to other messages
      case _ => 3
    }
  )
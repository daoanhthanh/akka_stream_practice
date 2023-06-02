package vn.flinters.testMQ

import akka.actor.{Actor, Props}
import akka.dispatch.ExecutionContexts.global
import vn.flinters.Utils._
import akka.pattern.Patterns.after

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

object MessageQueue extends App {

  private val actorWithPriorityMailbox = system.actorOf(
    Props[MyActor].withMailbox("my-mailbox"),
    name = "actorWithPriorityMailbox"
  )

  private class MyActor extends Actor {

    private var downloadRequestCount = 0
    private var downloadCompletedCount = 0
    private var isDownloadingCompleted = downloadRequestCount == downloadCompletedCount

    def receive: Receive = {
      case DownloadLink =>
        this.downloadRequestCount += 1
        high()

      case DownloadLinkCompleted => this.downloadCompletedCount += 1

      case ProcessDownloadedFiles => low()
      case message => println(s"Received $message")
    }

    private def high()(implicit ec: ExecutionContext): Unit = Future {
      Thread.sleep(3000)
      println("Download completed")
    }.onComplete {
      case Success(_) => self ! DownloadLinkCompleted
    }

    def networkRequestWithRetries(factor: Float = 1.5f, init: Int = 1, cur: Int = 0): Future[String] = {
      MockAPI.networkRequest().recoverWith {
        case NetworkException =>
          val next: Int =
            if (cur == 0) init
            else Math.ceil(cur * factor).toInt
          println(s"retrying after ${next} ms")
          after(next.milliseconds, system.scheduler, global, Future.successful(1)).flatMap { _ =>
            networkRequestWithRetries(factor, init, next)
          }

        case t: Throwable => throw t
      }
    }

    private def low()(implicit ec: ExecutionContext): Future[Unit] = Future {
      println("Processing completed")
    }
  }

  private case object DownloadLink

  private case object DownloadLinkCompleted

  private case object ProcessDownloadedFiles
  private case object Kill
  // Send messages to the actor
  actorWithPriorityMailbox ! DownloadLink
  actorWithPriorityMailbox ! DownloadLink
  actorWithPriorityMailbox ! DownloadLink
  actorWithPriorityMailbox ! DownloadLink
  actorWithPriorityMailbox ! ProcessDownloadedFiles

}
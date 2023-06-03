package vn.flinters.testMQ

import akka.actor.{Actor, Props}
import akka.pattern.after
import vn.flinters.Utils._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

case object DownloadingFileNotCompleted
    extends RuntimeException("All downloading creative is not completed!")

object MessageQueue extends App {

  val actorWithPriorityMailbox = system.actorOf(
    Props[MyActor].withMailbox("my-mailbox"),
    name = "actorWithPriorityMailbox"
  )

  private class MyActor extends Actor {

    private var downloadRequestCount = 0
    private var downloadCompletedCount = 0

    def receive: Receive = {
      case DownloadLink =>
        this.downloadRequestCount += 1
        high().onComplete {
          case Success(_) => self ! DownloadLinkCompleted
        }

      case DownloadLinkCompleted =>
        this.downloadCompletedCount += 1
        if (downloadRequestCount == downloadCompletedCount) {
          Okla()
        }

      case message => println(s"Received $message")
    }

    private def exponentialBackOff[T](retryTimes: Int = -1)(
        factor: Float = 1.5f,
        init: Int = 1,
        cur: Int = 0
    )(f: => Future[T]): Future[T] = {
      val next: Int =
        if (cur == 0) init
        else Math.ceil(cur * factor).toInt

      f.recoverWith {
        case DownloadingFileNotCompleted =>
          after(next.millis)(exponentialBackOff()(factor, init, next)(f))
        case t: Throwable => throw t
      }
    }

    def high(
        factor: Float = 1.5f,
        init: Int = 1,
        cur: Int = 0
    ): Future[String] = {
      MockAPI.networkRequest().recoverWith {
        case NetworkException =>
          val next: Int =
            if (cur == 0) init
            else Math.ceil(cur * factor).toInt
          println(s"retrying after ${next} ms")
          after(next.millis)(high(factor, init, next))

        case t: Throwable => throw t
      }
    }

    private def Okla(): Future[Unit] = Future {
      println("Processing completed")
    }
  }

  case object DownloadLink

  case object DownloadLinkCompleted

  case object ProcessDownloadedFiles

  private case object Kill
  // Send messages to the actor
  actorWithPriorityMailbox ! DownloadLink
  actorWithPriorityMailbox ! DownloadLink
  actorWithPriorityMailbox ! DownloadLink
  actorWithPriorityMailbox ! DownloadLink
}

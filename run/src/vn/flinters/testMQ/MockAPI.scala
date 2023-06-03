package vn.flinters.testMQ

import vn.flinters.testMQ.MessageQueue.{DownloadLinkCompleted, actorWithPriorityMailbox}

import scala.concurrent.duration.DurationInt
import scala.util.{Random, Success}
import scala.concurrent.{ExecutionContext, Future}

object NetworkException extends Throwable

object MockAPI {
  def networkRequest(): Future[String] = {
    if ((Random.nextFloat * 100) < 80)
      Future.failed(NetworkException)
    else
      Future.successful("Download successfully!")
  }

  def simpleRequest()(implicit ec: ExecutionContext): Future[Unit] = Future {
    Thread.sleep(3000)
    println("Download successfully")
  }
}
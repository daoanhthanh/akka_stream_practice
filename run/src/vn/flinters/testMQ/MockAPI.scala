package vn.flinters.testMQ

import scala.util.Random
import scala.concurrent.Future

object NetworkException extends Throwable

object MockAPI {
  def networkRequest(): Future[String] = {
    if ((Random.nextFloat * 100) < 80)
      Future.failed(NetworkException)
    else
      Future.successful("<data>")
  }
}
package vn.flinters.testMQ

import akka.actor.typed.ActorSystem
import akka.pattern.after

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import akka.actor.ActorSystem

object Retry {

  def exponentialBackOff[T](retryTime: Int = -1)(
      factor: Float = 1.5f,
      init: Int = 1,
      cur: Int = 0
  )(
      f: => Future[T]
  )(implicit ec: ExecutionContext, actorSystem: ActorSystem): Future[T] = {
    val next: Int =
      if (cur == 0) init
      else Math.ceil(cur * factor).toInt

    val remainRetryTime = retryTime - 1
    f.recoverWith { case t: Throwable =>
      if (remainRetryTime <= 0) throw t
      else
        after(next.millis)(
          exponentialBackOff(remainRetryTime)(factor, init, next)(f)(
            ec,
            actorSystem
          )
        )(actorSystem)
    }
  }
}

package vn.flinters

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.ExecutionContextExecutor

object Utils {
  implicit val system: ActorSystem = ActorSystem("Main")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val mt: Materializer = ActorMaterializer()
}
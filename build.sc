import mill._
import mill.scalalib._

object run extends ScalaModule {

  def scalaVersion = "2.12.17"

  val AkkaVersion = "2.8.2"

  override def ivyDeps = Agg(
    ivy"org.scalameta::munit::0.7.29",
    ivy"com.typesafe.akka::akka-stream:$AkkaVersion",
    ivy"com.softwaremill.retry::retry:0.3.6",
    ivy"com.google.cloud:google-cloud-storage:2.20.1",
    ivy"com.google.cloud:google-cloud-datastore:2.14.6",
    ivy"com.typesafe.play::play-ahc-ws-standalone:2.0.0",
    ivy"com.typesafe.akka::akka-actor-typed:$AkkaVersion"
  )

  object test extends Tests with TestModule.Munit {
    override def ivyDeps = Agg(
      ivy"org.scalameta::munit::0.7.29"
    )
  }
}
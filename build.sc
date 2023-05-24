import mill._
import mill.scalalib._

object run extends ScalaModule {

  def scalaVersion = "2.13.10"

  val AkkaVersion = "2.8.2"

  override def ivyDeps = Agg(
    ivy"org.scalameta::munit::0.7.29",
    ivy"com.typesafe.akka::akka-stream:$AkkaVersion",
    ivy"com.softwaremill.retry::retry:0.3.6",
  )

  object test extends Tests with TestModule.Munit {
    override def ivyDeps = Agg(
      ivy"org.scalameta::munit::0.7.29"
    )
  }
}
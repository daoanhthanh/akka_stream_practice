package vn.flinters

import akka.{Done, NotUsed}
import akka.stream._
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, GraphDSL, Keep, Sink, Source}
import akka.util.ByteString
import vn.flinters.Utils._

import java.nio.file.Paths
import scala.concurrent.Future
import scala.language.postfixOps

object Content {
  private val source = Source(1 to 10)

  def testGraph(): Unit = {
    val decider: Supervision.Decider = {
      case _: AhihiExeption => Supervision.Resume
      case _ => Supervision.Stop
    }
    val des1: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get("sink1.txt"))
    val des2: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get("sink2.txt"))
    val des3: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get("sink3.txt"))
    val des4: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get("sink4.txt"))
    val des5: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get("sink5.txt"))


    /** Flows**/
    val flow1 =
      Flow[Int].map { x =>
        if (x == 7) throw AhihiExeption("Error")
        else x
      }
    val flow2 = Flow[Int].map(_ * 2)
    val flow4 = Flow[String].map(ByteString(_))



    val sink = Sink.fromGraph(
      GraphDSL.createGraph(des1, des2, des3, des4, des5)(
        (m1, m2, m3, m4, m5) => Future.sequence(Seq(m1, m2, m3, m4, m5))
      ) { implicit builder: GraphDSL.Builder[Future[Seq[IOResult]]] =>
        (des1, des2, des3, des4, des5) =>
          import GraphDSL.Implicits._

          val bc = builder.add(Broadcast[Int](5))


          bc.out(0) ~> flow1 ~> flow2 ~> flow3(1) ~> flow4 ~> des1
          bc.out(1) ~> flow2 ~> flow3(2) ~> flow4 ~> des2
          bc.out(2) ~> flow1 ~> flow2 ~> flow3(3) ~> flow4 ~> des3
          bc.out(3) ~> flow1 ~> flow2 ~> flow3(4) ~> flow4 ~> des4
          bc.out(4) ~> flow1 ~> flow2 ~> flow3(5) ~> flow4 ~> des5

          SinkShape(bc.in)
      })

    source.toMat(sink)(Keep.right)
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .run()
      .onComplete { _ =>
        system.terminate()
      }

  }

  private def flow3(num: Int): Flow[Int, String, NotUsed] = Flow[Int].map(s"Final number of flow $num is: " + _.toString + "\n")

  def testAlsoVia(): Unit = {

    val sink = Sink.foreach(println)
    val sink2: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get("also_to_this.txt"))

    val a: (Future[Done], Future[IOResult]) = source
      .alsoToMat(sink)(Keep.right)
      .map(s => ByteString(s.toString + "\n"))
      .toMat(sink2)(Keep.both)
      .withAttributes(ActorAttributes.supervisionStrategy(Supervision.resumingDecider))
      .run()
  }

  case class AhihiExeption(msg: String) extends Exception()

}
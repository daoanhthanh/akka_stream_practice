//package vn.flinters
//
//import akka.NotUsed
//import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source}
//import akka.stream.{ActorAttributes, Attributes}
//import akka.util.ByteString
//import play.api.libs.ws.ahc.StandaloneAhcWSClient
//import play.shaded.ahc.org.asynchttpclient.{DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig}
//import vn.flinters.Utils._
//
//
//import java.nio.file.Paths
//import java.security.MessageDigest
//import scala.concurrent.Future
//import scala.util.Success
//
//
//class NonRetryableException(message: String) extends Exception(message)
//
//case class UrlNotFoundException(message: String) extends NonRetryableException(message)
//
//case class UrlForbiddenException(message: String, responseHeaders: Map[String, scala.collection.Seq[String]], responseBody: String)
//  extends NonRetryableException(message)
//
//
//object DownloadMediaWithHash extends App {
//
//  final val hexCode = "0123456789abcdef".toCharArray;
//  val config = new DefaultAsyncHttpClientConfig.Builder()
//    .setReadTimeout(10 * 60 * 1000)
//    .setRequestTimeout(10 * 60 * 1000)
//    .build()
//  val ws = new StandaloneAhcWSClient(new DefaultAsyncHttpClient(config))
//  private val url = "https://obs.line-scdn.net/0hIdjfo_jTFktXHwDsSH1pHGhPHTpkcgFIN2cEfSdDJn5-JlUea3teLXcYTnsqLlkUY35faHNKHyl4J1IZ"
//  private val channelAttrs: Attributes = Attributes.name("ChannelConverters") and ActorAttributes.IODispatcher
//  private val sha256 = Flow[ByteString]
//    .fold(MessageDigest.getInstance("SHA-256"))((md, bs) => {
//      md.update(bs.toArray)
//      md
//    })
//    .map(md => ByteString(md.digest()))
//    .toMat(Sink.head)(Keep.right)
//  private val expectedContentHash = "a67a5f56f6e03460bbb213ed5339ef2deba7a2e01c9f656c5e71b1eb9d56d8f1"
//
//  private def printHexBinary(data: Array[Byte]): String = {
//    val r: StringBuilder = new StringBuilder(data.length * 2);
//    for (b <- data) {
//      r.append(hexCode((b >> 4) & 0xF));
//      r.append(hexCode(b & 0xF));
//    }
//
//    r.toString()
//  }
//
//  private def downloadFromHttpAsStream(url: String): Future[(Source[ByteString, NotUsed], Option[String])] = {
//    ws.url(url).withFollowRedirects(true).stream().flatMap { resp =>
//      if (resp.status / 100 == 2) {
//        Future.successful((resp.bodyAsSource.mapMaterializedValue(_ => NotUsed), resp.header("Content-Type")))
//      } else if (resp.status == 403) {
//        resp.bodyAsSource
//          .runFold(ByteString.empty) { case (acc, str) => acc concat str }
//          .map(_.utf8String)
//          .flatMap { body =>
//            Future.failed(UrlForbiddenException(s"指定のURLへのアクセスは禁止されています url: $url", resp.headers, body))
//          }
//      } else if (resp.status == 404 || resp.status == 410) {
//        Future.failed(UrlNotFoundException(s"指定のURLは存在しません url: $url status: ${resp.status}"))
//      } else {
//        Future.failed(new RuntimeException(s"指定のURLへのアクセスに失敗しました url: $url status: ${resp.status}"))
//      }
//    }
//  }
//
//
//  FileIO.fromPath(Paths.get("data.jpeg"))
//    .toMat(sha256)((_, r) => r)
//    .run()
//    .onComplete {
//      case Success(value) => println(s"Read from file: $value")
//    }
//
//  private def readFileByChunks(filePath: String, chunkSize: Int = 8192): Array[Byte] = {
//    val source = scala.io.Source.fromFile(filePath, "UTF-8")
//    try {
//      val iterator = source.grouped(chunkSize)
//
//      if (iterator.hasNext) {
//        val a = iterator.next()
//
//        println(a)
//        a.map(_.toByte).toArray
//      } else {
//        Array.empty
//      }
//    } finally {
//      println("emppty")
//      source.close()
//    }
//  }
//
//  private case class ContentHash(hash: ByteString) {
//
//    override def toString: String = DatatypeConverter.printHexBinary(hash.toArray).toLowerCase
//
//  }
//  val md = MessageDigest.getInstance("SHA-256")
//
//  md.update(
//    url.toCharArray.map(_.toByte)
//  )
//  println(s"Custom contenthash: ${
//    printHexBinary(md.digest())
//  }")
//
//
//  downloadFromHttpAsStream(url)
//    .flatMap {
//      case (contentSrc, _) =>
//        val sha = contentSrc.toMat(sha256)(Keep.right).run()
//        sha.map(ContentHash)
//    }
//    .onComplete(s => {
//      println(s"Expected: $expectedContentHash")
//      println("Result: " + s.get)
//      println("content" + s.get.hash.toArray.mkString("Array(", ", ", ")"))
//      println("New type of converter 2 using String builder:" + printHexBinary(s.get.hash.toArray))
//      println("Is equal: " + (expectedContentHash == s.get.toString))
//      ws.close()
//      system.terminate()
//    })
//}
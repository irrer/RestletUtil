package edu.umro.RestletUtilTest

import edu.umro.RestletUtil.HttpsClient
import edu.umro.ScalaUtil.{FileUtil, Logging}

import java.io.ByteArrayOutputStream

object TestHttpsClient_httpGet extends Logging {
  def main(args: Array[String]): Unit = {

    if (args.length != 3) {
      println("Failure.  Must be run with parameters: url userId password")
      System.exit(1)
    }

    val url = args.head
    val userId = args(1)
    val password = args(2)

    println(s"url: $url    userId: $userId")

    val challengeResponse = HttpsClient.makeChallengeResponse(userId = userId, password = password)

    val clientResource = HttpsClient.makeClientResource(url, Some(challengeResponse), trustKnownCertificates = true)

    var size: Int = -1

    val start = System.currentTimeMillis()
    for (_ <- 0 until 100) {
      HttpsClient.httpsGet(clientResource, url) match {
        case Left(throwable) =>
          println(s"Failed to get $url: ${fmtEx(throwable)}")
          System.exit(1)
        case Right(representation) =>
          val bos = new ByteArrayOutputStream()
          val input = representation.getStream
          FileUtil.copyStream(input, bos)
          val data = bos.toByteArray
          if (size < 0) {
            size = data.length
          }
          if (size != data.length) {
            println(s"$url failed with ${data.length} bytes when $size was expected.")
            System.exit(1)
          } else {
            println(s"$url succeeded with $size bytes.")
          }
      }
    }
    val elapsed = System.currentTimeMillis() - start
    println(s"Success!  elapsed ms: $elapsed")
  }
}

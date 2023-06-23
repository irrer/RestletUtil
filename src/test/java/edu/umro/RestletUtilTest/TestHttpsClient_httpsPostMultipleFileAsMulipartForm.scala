package edu.umro.RestletUtilTest

import edu.umro.RestletUtil.HttpsClient
import edu.umro.ScalaUtil.Logging
import org.restlet.data.MediaType

import java.io.File

object TestHttpsClient_httpsPostMultipleFileAsMulipartForm extends Logging {
  def main(args: Array[String]): Unit = {

    if (args.length != 3) {
      logger.info("Failure.  Must be run with parameters: url userId password")
      System.exit(1)
    }

    val url = args.head
    val userId = args(1)
    val password = args(2)

    logger.info(s"url: $url    userId: $userId")

    val challengeResponse = HttpsClient.makeChallengeResponse(userId = userId, password = password)

    val clientResource = HttpsClient.makeClientResource(url, Some(challengeResponse), trustKnownCertificates = true)

    var size: Int = -1

    val fileList = {
      Seq(
        new File("""src/test/resources/upload1.xml"""),
        new File("""src/test/resources/upload2.xml""")
      )
    }

    val start = System.currentTimeMillis()

    // val data: Array[Byte] = FileUtil.readBinaryFile(new File("""src/test/resources/upload1.xml""")).right.get

    val postResult = HttpsClient.httpsPostMultipleFilesAsMulipartForm(
      clientResource,
      url,
      fileList,
      MediaType.APPLICATION_XML
    )

    postResult match {
      case Left(throwable) =>
        logger.info(s"Failed to post $url: ${fmtEx(throwable)}")
        System.exit(1)
      case Right(_) =>
        logger.info(s"$url succeeded")
    }

    val elapsed = System.currentTimeMillis() - start
    logger.info(s"Success!  elapsed ms: $elapsed")
  }
}

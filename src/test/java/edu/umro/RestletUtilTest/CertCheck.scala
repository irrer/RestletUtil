package edu.umro.RestletUtilTest

import edu.umro.RestletUtil.HttpsClient

import java.io.IOException

object CertCheck {
  private var passCount = 0
  private var failCount = 0

  private def test(url: String, pass: Boolean): Unit = {
    val clientResource = HttpsClient.makeClientResource(url = url)
    //val clientResource = new ClientResource(new Context, url)
    // clientResource.setChallengeResponse(challengeResponse);
    try {
      val representation = clientResource.get
      val text = representation.getText
      System.out.println("----------\n" + text.substring(0, 100) + "\n----------")
      if (pass) {
        System.out.println("pass")
        passCount += 1
      } else {
        System.out.println("fail")
        failCount += 1
      }
    } catch {
      case e: Throwable =>
        System.out.println("Failed: " + e.getMessage)
        if (pass) {
          System.out.println("fail")
          failCount += 1
        } else {
          System.out.println("pass")
          passCount += 1
        }
    }
  }

  @throws[IOException]
  def main(args: Array[String]): Unit = {
    System.out.println("Starting...")

    // ChallengeResponse challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "userID", "password");
    val passList = Array(
      "https://mobius2.med.umich.edu",
      "https://automatedqualityassurance.org",
      "https://www.google.com/",
      "https://badssl.com/"
    )

    val failList = Array(
      "https://self-signed.badssl.com",
      "https://expired.badssl.com/",
      "https://untrusted-root.badssl.com/",
      "https://automatedqualityassurance.org/view/OutputList"
    ) // this will fail

    for (url <- passList) {
      test(url, pass = true)
    }
    for (url <- failList) {
      test(url, pass = false)
    }
    System.out.println("Finished.   passCount: " + passCount + "    failCount: " + failCount)
  }

}

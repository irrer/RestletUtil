package edu.umro.RestletUtilTest

import org.restlet.data.ChallengeScheme
import edu.umro.RestletUtil.HttpsClient

/**
 * Test and provide examples for HttpsClient.
 */
object TestHttpsClient {

  def main(args: Array[String]): Unit = {
    val test1 = HttpsClient.httpsGet("http://www-personal.umich.edu/~irrer/tmp/readinglist.html") // HTTP does not need user and password
    val test2 = HttpsClient.httpsGet("https://automatedqualityassurance.org") // HTTPS does not need user and password
    val test3 = HttpsClient.httpsGet("https://automatedqualityassurance.org", "someuser", "theirpassword") // HTTPS does not need user and password
    val test4 = HttpsClient.httpsGet("https://automatedqualityassurance.org/admin/MachineTypeList", "irrer", "notmyrealpassword") // HTTPS needs valid user and password
    val test5 = HttpsClient.httpsGet("https://automatedqualityassurance.org/admin/MachineTypeList", "irrer", "notmyrealpassword", ChallengeScheme.HTTP_BASIC) // Fully specified invocation.  HTTPS needs valid user and password

    for (test <- Seq(test1, test2, test3, test4, test5)) {
      println("-----------------------------------------------------------------------")
      if (test.isRight) test.right.get.write(System.out)
      else println("Error: " + test.left.get)
    }
  }

}
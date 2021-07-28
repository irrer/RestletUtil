/*
 * Copyright 2021 Regents of the University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
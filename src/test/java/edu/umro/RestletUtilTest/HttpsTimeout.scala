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

import edu.umro.ScalaUtil.Trace
import org.restlet.{Client, Context}
import org.restlet.data.Protocol
import org.restlet.resource.ClientResource

import java.io.File

/**
 * Learn about HTTPS client timeouts.
 */

object HttpsTimeout {

  /**
   * For testing and development only.
   */
  def main(args: Array[String]): Unit = {
    println("Starting")
    val start = System.currentTimeMillis

    if (false) {
      val cl = new Client(Protocol.HTTPS)
      val ctx = cl.getContext
      ctx.setParameters(???)
    }

    if (true) {
      // from Thierry
      val client = new Client(new Context, Protocol.HTTPS)
      val old = client.getContext.getParameters
      Trace.trace(old)
      client.getContext.getParameters.add("socketTimeout", "1000")
      Trace.trace(old)

      val cr = new ClientResource("http://example.com")
      cr.setNext(client)
      System.exit(0)
    }

    val file: File = new File("""D:\tmp\aqa\CBCT\MQATX2OBI2019Q3\RTIMAGE_19.zip""")

    val httpParams = "?Run=Run&AutoUpload=true&Await=true"
    // val result = HttpsClient.httpsPostSingleFileAsMulipartForm("https://localhost:9443/run/BBbyEPID_7" + httpParams, file, MediaType.APPLICATION_ZIP, "irrer", "45eetslp", ChallengeScheme.HTTP_BASIC, true)
    // Trace.trace(result)

    val elapsed = System.currentTimeMillis - start
    println("\nExiting.  Elapsed ms: " + elapsed)
    System.exit(0)
  }

}
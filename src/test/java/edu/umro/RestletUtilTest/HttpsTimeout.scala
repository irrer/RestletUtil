package edu.umro.RestletUtilTest

import org.restlet.Application
import org.restlet.Request
import org.restlet.Response
import org.restlet.data.MediaType
import org.restlet.data.Status
import org.restlet.routing.Router
import org.restlet.Restlet
import org.restlet.Component
import java.io.File
import java.util.Date
import org.restlet.routing.Template
import edu.umro.RestletUtil.RestletHttps
import org.restlet.Client
import org.restlet.data.Protocol
import org.restlet.Context
import org.restlet.resource.ClientResource
import edu.umro.RestletUtil.HttpsClient
import org.restlet.data.ChallengeScheme
import edu.umro.ScalaUtil.Trace

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
    val result = HttpsClient.httpsPostSingleFileAsMulipartForm("https://localhost:9443/run/BBbyEPID_7" + httpParams, file, MediaType.APPLICATION_ZIP, "irrer", "45eetslp", ChallengeScheme.HTTP_BASIC, true)
    Trace.trace(result)

    val elapsed = System.currentTimeMillis - start
    println("\nExiting.  Elapsed ms: " + elapsed)
    System.exit(0)
  }

}
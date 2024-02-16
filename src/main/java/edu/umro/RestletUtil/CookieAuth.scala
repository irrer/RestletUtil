package edu.umro.RestletUtil

import edu.umro.ScalaUtil.Logging
import edu.umro.ScalaUtil.Trace
import org.restlet.Context
import org.restlet.Response
import org.restlet.data.Cookie
import org.restlet.data.CookieSetting
import org.restlet.data.Form
import org.restlet.data.MediaType
import org.restlet.representation.StringRepresentation
import org.restlet.resource.ClientResource
import org.restlet.util.Series

import java.io.CharArrayWriter

/*
 * Stand-alone test to verify that we can talk to both Mobius servers via HTTPS.
 *
 * Run as:
 *
 *     java -cp RestletUtil_2.12-0.1.2-jar-with-dependencies.jar edu.umro.RestletUtil.CookieAuth mobius2.med.umich.edu
 *
 * if no parameter is specified, then it defaults to mobius.med.umich.edu
 *
 * ------------------------------------------------------------------------------------------------------------------------------------------------------------------
 *
 * The following command was used to add the required certificate for Mobius.  It is a higher level one that was used to sign the certificates for both Mobius systems.
 *
 * The cacert file is in the lib/security directory of the java installation.
 *
 *     keytool -importcert -keystore cacerts -storepass changeit -file InCommon_RSA_Server_CA_2_USERTrust_RSA_Certification_Authority_.cer -alias "InCommon_RSA_Server_CA_2_USERTrust_RSA_Certification_Authority_.cer"
 *
 */

object CookieAuth extends Logging {

  private def cookieSettingListToCookieList(cookieSettingSeries: Series[CookieSetting]): Seq[Cookie] = {
    val nameValueMap = (0 until cookieSettingSeries.size)
      .map(i => {
        val cs = cookieSettingSeries.get(i)
        (cs.getName, cs.getValue)
      })
      .toMap

    val distinctCookieList = nameValueMap.keys.map(name => new Cookie(name, nameValueMap(name)))

    distinctCookieList.toSeq
  }

  /**
    * Read all of the data from a response.
    * @param response Read from here.
    * @return A byte array representing the results.
    */
  private def responseData(response: Response): Array[Byte] = {
    val entity = response.getEntity

    if ((entity != null) && entity.isAvailable) {
      val osw = new CharArrayWriter()
      entity.write(osw)
      val c = osw.toCharArray
      val ba = c.toSeq.map(_.toByte).toArray
      ba
    } else
      Array() // no data
  }

  def main(args: Array[String]): Unit = {
    System.out.println("Starting...")
    try {

      // val hostName = "mobius.med.umich.edu"
      val hostName = {
        if (args.nonEmpty)
          args.head
        else
          "mobius.med.umich.edu"
      }
      println(s"contacting host: $hostName")
      val loginUrl = s"https://$hostName/auth/login"
      val username = "MobiusControl"
      val password = "Mcp4admin2use"
      val usernameTag = "username"
      val passwordTag = "password"

      val clientResource: ClientResource = new ClientResource(new Context, loginUrl)
      // val challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_COOKIE, username, password)
      // clientResource.setChallengeResponse(challengeResponse)

      val form = new Form()
      form.add(usernameTag, username)
      form.add(passwordTag, password)

      val representation = new StringRepresentation("")
      representation.setMediaType(MediaType.APPLICATION_WWW_FORM)
      representation.setCharacterSet(null)
      println(s"representation: $representation")

      Trace.trace()
      clientResource.post(form)
      Trace.trace()

      val response = clientResource.getResponse

      println(s"response: $response")
      Trace.trace()

      val cookieSettingSeries = response.getCookieSettings

      val cookies = cookieSettingListToCookieList(cookieSettingSeries)
      Trace.trace(cookies)

      // ----------------------------------------------------------------

      val jsonUrl = s"https://$hostName/_plan/list?sort=date&descending=1&format=json&limit=20"

      val cr = new ClientResource(new org.restlet.Context, jsonUrl)
      val requestCookieList = cr.getRequest.getCookies
      requestCookieList.clear()
      cookies.map(c => requestCookieList.add(c))
      cr.get()
      val status = cr.getResponse.getStatus
      val data: Option[Array[Byte]] = if (status.isSuccess) {
        logger.info("Successfully performed HTTP GET from: " + jsonUrl + "    Status: " + cr.getResponse.getStatus)
        val d = responseData(cr.getResponse)
        Some(d)
      } else {
        logger.error("Failed to perform HTTP GET from: " + jsonUrl + "    Status: " + cr.getResponse.getStatus)
        None
      }
      cr.release()
      val text = new String(data.get)
      Trace.trace(text.take(500))
      Trace.trace()

    } catch {
      case e: Throwable =>
        System.out.println("Failed:\n" + fmtEx(e))
    }

    System.out.println("Finished.")
  }

}

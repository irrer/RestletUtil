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

package edu.umro.RestletUtil

import edu.umro.ScalaUtil.Trace
import org.restlet.Context
import org.restlet.data._
import org.restlet.ext.html.{FormData, FormDataSet}
import org.restlet.representation.{ByteArrayRepresentation, FileRepresentation, Representation, StringRepresentation}
import org.restlet.resource.ClientResource

import java.io.File
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Client support for HTTPS and HTTP.
  *
  * While these functions can be useful, they are relatively trivial and somewhat constrictive.  Their most
  * frequent could very well be to to serve as examples of how to call a web server.
  */
object HttpsClient {

  def makeChallengeResponse(scheme: ChallengeScheme = ChallengeScheme.HTTP_BASIC, userId: String = "", password: String = ""): ChallengeResponse = {
    val challengeResponse = new ChallengeResponse(scheme, userId, password)
    challengeResponse
  }

  /**
    * Establish the client resource, setting up the certificate trust model as the caller specified.
    *
    * @param url: URL of service
    * @param trustKnownCertificates: If true, select the list of certificates specified with the <code>TrustKnownCertificates.init</code>
    * function. Defaults to false.
    */
  //noinspection ScalaWeakerAccess
  def makeClientResource(
      url: String,
      challengeResponse: Option[ChallengeResponse] = None,
      trustKnownCertificates: Boolean = false,
      parameterList: Map[String, String] = Map[String, String](),
      cookieList: Seq[Cookie] = Seq[Cookie]()
  ): ClientResource = {

    val clientResource: ClientResource = new ClientResource(new Context, url)

    if (trustKnownCertificates) {
      clientResource.getContext.getAttributes.put("sslContextFactory", new TrustingSslContextFactory)
    }

    if (challengeResponse.nonEmpty)
      clientResource.setChallengeResponse(challengeResponse.get)

    if (parameterList.nonEmpty) {
      val parameters = clientResource.getContext.getParameters
      parameterList.keys.map(key => parameters.add(key, parameterList(key)))
    }

    if (cookieList.nonEmpty) {
      val requestCookieList = clientResource.getRequest.getCookies
      requestCookieList.clear()
      cookieList.map(c => requestCookieList.add(c))
    }
    clientResource
  }

  private def perform(op: () => Representation, timeout_ms: Option[Long] = None): Either[Throwable, Representation] = {
    try {
      if (timeout_ms.isDefined) {
        val duration = Duration(timeout_ms.get, TimeUnit.MILLISECONDS)
        val future = Future { op() }
        val res = Await.result(future, duration)
        Right(res)
      } else Right(op())
    } catch {
      case t: Throwable =>
        Left(t)
    }
  }

  /**
    * Fetch content via HTTPS or HTTP.  If there is a security failure then
    * a <code>CertificateException</code> is thrown.
    *
    * @param clientResource: Connection
    * @param url           : URL of service
    * @param timeout_ms    : If defined, timeout after this many ms.
    */
  def httpsGet(
      clientResource: ClientResource,
      url: String,
      timeout_ms: Option[Long] = None
  ): Either[Throwable, Representation] = {
    clientResource.setReference(url)
    perform(clientResource.get _, timeout_ms)
  }

  /**
    * Post content via HTTPS or HTTP.
    *
    * @param clientResource: Connection
    * @param url: URL of service
    * @param data: Content to post
    * @param timeout_ms: If defined, timeout after this many ms.
    */
  //noinspection ScalaUnusedSymbol
  private def httpsPost(
      clientResource: ClientResource,
      url: String,
      data: Array[Byte],
      timeout_ms: Option[Long] = None
  ): Either[Throwable, Representation] = {
    val representation = new ByteArrayRepresentation(data)
    clientResource.setReference(url)
    def op = clientResource.post(representation)
    perform(op _, timeout_ms)
  }

  /**
    * Post content via HTTPS or HTTP.
    *
    * @param clientResource: Connection
    * @param url: URL of service
    * @param file: Content to post
    * @param timeout_ms: If defined, timeout after this many ms.
    */
  //noinspection ScalaUnusedSymbol
  private def httpsPostZipFile(
      clientResource: ClientResource,
      url: String,
      file: File,
      timeout_ms: Option[Long] = None
  ): Either[Throwable, Representation] = {
    clientResource.setReference(url)
    val representation = new FileRepresentation(file, MediaType.APPLICATION_ZIP)
    def op = clientResource.post(representation)
    perform(op _, timeout_ms)
  }

  /**
    * Post a files as a form set data.
    *
    * @param clientResource: Connection
    * @param url           : URL of service
    * @param file          : Post this file
    * @param fileMediaType : Media type (zip, xml, etc).
    * @param timeout_ms:   : If defined, timeout after this many ms.
    */
  //noinspection SpellCheckingInspection
  def httpsPostSingleFileAsMulipartForm(
      clientResource: ClientResource,
      url: String,
      file: File,
      fileMediaType: MediaType,
      timeout_ms: Option[Long] = None
  ): Either[Throwable, Representation] = {

    clientResource.setReference(url)
    val entity = new FileRepresentation(file, fileMediaType) //create the fileRepresentation

    val fds = new FormDataSet
    fds.getEntries.add(new FormData("FileTag", entity))
    fds.setMultipart(true)

    def op = clientResource.post(fds, MediaType.MULTIPART_FORM_DATA)
    perform(op _, timeout_ms)
  }

  /**
    * Post multiple files as form set data.
    *
    * @param clientResource: Connection
    * @param url           : post to here
    * @param fileList      : Post these files in this order
    * @param fileMediaType : Media type (same for all files).  If the files do not have all the same media type, then this function will not work.
    * @param timeout_ms    : If defined, timeout after this many ms.
    */
  //noinspection ScalaWeakerAccess,SpellCheckingInspection
  def httpsPostMultipleFilesAsMulipartForm(
      clientResource: ClientResource,
      url: String,
      fileList: Seq[File],
      fileMediaType: MediaType,
      timeout_ms: Option[Long] = None
  ): Either[Throwable, Representation] = {

    var inc = 1
    val fds = new FormDataSet
    for (file <- fileList) {
      val entity = new FileRepresentation(file, fileMediaType) //create the fileRepresentation
      fds.getEntries.add(new FormData("FileTag" + inc, entity))
      inc = inc + 1
      fds.setMultipart(true)
    }

    def op = clientResource.post(fds, MediaType.MULTIPART_FORM_DATA)
    perform(op _, timeout_ms)
  }

  /**
    * Authenticate via cookies.
    *
    * @param url URL of login page.
    * @param usernameTag Name of parameter specifying user name.
    * @param username User name.
    * @param passwordTag Name of parameter specifying password.
    * @param password Password.
    * @return List of cookies to be used in subsequent HTTP calls.
    */
  //noinspection ScalaWeakerAccess
  def AuthenticateViaCookies(url: String, usernameTag: String, username: String, passwordTag: String, password: String): Seq[Cookie] = {
    val clientContext = new org.restlet.Context
    clientContext.getAttributes.put("sslContextFactory", new TrustingSslContextFactory)

    val clientResource = new ClientResource(clientContext, url)
    val form = new Form()
    form.add(usernameTag, username)
    form.add(passwordTag, password)

    val representation = new StringRepresentation("")
    representation.setMediaType(MediaType.APPLICATION_WWW_FORM)
    representation.setCharacterSet(null)

    clientResource.post(form)

    val response = clientResource.getResponse

    val cookieSettings = response.getCookieSettings
    println("cookieSettings.size: " + cookieSettings.size)
    for (i <- 0 until cookieSettings.size) {
      val cs = cookieSettings.get(i)
      println("\n  cs " + i + " : " + cs)
      println("  cookie setting " + i + " : " + cs.getName + " --> " + cs.getValue)
    }

    val seq = (0 until cookieSettings.size)
      .map(i => {
        val cs = cookieSettings.get(i)
        (cs.getName, cs.getValue)
      })
      .toMap

    val cookieSeq = seq.map(nv => new Cookie(nv._1, nv._2)).toSeq.sortBy(_.getName)

    cookieSeq
  }

  def main(args: Array[String]): Unit = { //
    Trace.trace("starting")
    val url = "http://10.30.65.121/" + "auth/login"

    val username = "yourUserName"
    val password = "yourPassword"

    val cookieList = AuthenticateViaCookies(url, "username", username, "password", password)

    Trace.trace("Number of cookies: " + cookieList.size)

    cookieList.foreach(c => Trace.trace("    " + c.getName + " --> " + c.getValue))

    Trace.trace("done")
  }

}

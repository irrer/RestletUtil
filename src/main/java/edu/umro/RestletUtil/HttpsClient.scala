package edu.umro.RestletUtil

import org.restlet.resource.ClientResource
import org.restlet.data.ChallengeResponse
import org.restlet.data.ChallengeScheme
import org.restlet.resource.ResourceException
import org.restlet.representation.Representation
import org.restlet.representation.ByteArrayRepresentation
import java.io.File
import org.restlet.representation.FileRepresentation
import org.restlet.data.MediaType
import edu.umro.ScalaUtil.FileUtil
import org.restlet.ext.html.FormDataSet
import org.restlet.ext.html.FormData
import org.restlet.data.Form
import com.sun.org.apache.bcel.internal.generic.FMUL
import edu.umro.ScalaUtil.Trace

/**
 * Client support for HTTPS and HTTP.
 *
 * While these functions can be useful, they are relatively trivial and somewhat constrictive.  Their most
 * frequent could very well be to to serve as examples of how to call a web server.
 */
object HttpsClient {

  /**
   * Establish the client resource, setting up the certificate trust model as the caller specified.
   *
   * @param url: URL of service
   *
   * @param trustKnownCertificates: If true, select the list of certificates specified with the <code>TrustKnownCertificates.init</code>
   * function. Defaults to false.
   */
  private def getClientResource(url: String, trustKnownCertificates: Boolean, parameterList: Map[String, String]) = {
    val clientResource = if (trustKnownCertificates) {
      val clientContext = new org.restlet.Context
      clientContext.getAttributes.put("sslContextFactory", new TrustingSslContextFactory)
      new ClientResource(clientContext, url)
    } else new ClientResource(url)

    if (parameterList.nonEmpty) {
      val parameters = clientResource.getContext.getParameters
      parameterList.keys.map(key => parameters.add(key, parameterList(key)))
    }

    clientResource
  }

  /**
   * Fetch content via HTTPS or HTTP.  If there is a security failure then
   * a <code>CertificateException</code> is thrown.
   *
   * @param url: URL of service
   *
   * @param userId: User id to authenticate with.  If not needed, then it need not be given or be an empty string.
   *
   * @param password: Password to authenticate with.  If not needed, then it need not be given or be an empty string.
   *
   * @param challengeScheme: Defaults to ChallengeScheme.HTTP_BASIC.
   *
   * @param trustKnownCertificates: If true, select the list of certificates specified with the <code>TrustKnownCertificates.init</code>
   * function. Defaults to false.
   */
  def httpsGet(
    url: String,
    userId: String = "",
    password: String = "",
    challengeScheme: ChallengeScheme = ChallengeScheme.HTTP_BASIC,
    trustKnownCertificates: Boolean = false,
    parameterList: Map[String, String] = Map()): Either[ResourceException, Representation] = {

    val clientResource = getClientResource(url, trustKnownCertificates, parameterList)
    val challengeResponse = new ChallengeResponse(challengeScheme, userId, password)
    clientResource.setChallengeResponse(challengeResponse)
    try {
      Right(clientResource.get)
    } catch {
      case re: ResourceException => {
        Left(re)
      }
    }
  }

  /**
   * Post content via HTTPS or HTTP.
   *
   * @param url: URL of service
   *
   * @param data: Content to post
   *
   * @param userId: User id to authenticate with.  If not needed, then it need not be given or be an empty string.
   *
   * @param password: Password to authenticate with.  If not needed, then it need not be given or be an empty string.
   *
   * @param challengeScheme: Defaults to ChallengeScheme.HTTP_BASIC.
   *
   * @param trustKnownCertificates: If true, select the list of certificates specified with the <code>TrustKnownCertificates.init</code>
   * function. Defaults to false.
   */
  def httpsPost(
    url: String,
    data: Array[Byte],
    userId: String = "",
    password: String = "",
    challengeScheme: ChallengeScheme = ChallengeScheme.HTTP_BASIC,
    trustKnownCertificates: Boolean = false,
    parameterList: Map[String, String] = Map()): Either[ResourceException, Representation] = {
    val clientResource = getClientResource(url, trustKnownCertificates, parameterList)
    val challengeResponse = new ChallengeResponse(challengeScheme, userId, password)
    clientResource.setChallengeResponse(challengeResponse)
    try {
      val representation = new ByteArrayRepresentation(data)
      Right(clientResource.post(representation))
    } catch {
      case re: ResourceException => {
        Left(re)
      }
    }
  }

  /**
   * Post content via HTTPS or HTTP.
   *
   * @param url: URL of service
   *
   * @param data: Content to post
   *
   * @param userId: User id to authenticate with.  If not needed, then it need not be given or be an empty string.
   *
   * @param password: Password to authenticate with.  If not needed, then it need not be given or be an empty string.
   *
   * @param challengeScheme: Defaults to ChallengeScheme.HTTP_BASIC.
   *
   * @param trustKnownCertificates: If true, select the list of certificates specified with the <code>TrustKnownCertificates.init</code>
   * function. Defaults to false.
   */
  def httpsPostZipFile(
    url: String,
    file: File,
    userId: String = "",
    password: String = "",
    challengeScheme: ChallengeScheme = ChallengeScheme.HTTP_BASIC,
    trustKnownCertificates: Boolean = false,
    parameterList: Map[String, String] = Map()): Either[ResourceException, Representation] = {
    val clientResource = getClientResource(url, trustKnownCertificates, parameterList)
    val challengeResponse = new ChallengeResponse(challengeScheme, userId, password)
    clientResource.setChallengeResponse(challengeResponse)
    try {
      val representation = new FileRepresentation(file, MediaType.APPLICATION_ZIP)
      // val representation = new FileRepresentation(file, MediaType.APPLICATION_ALL)
      //val representation = new FileRepresentation(file, MediaType.MULTIPART_FORM_DATA)
      Right(clientResource.post(representation))
    } catch {
      case re: ResourceException => {
        Left(re)
      }
    }
  }

  /**
   * Post content via HTTPS or HTTP.
   *
   * @param url: URL of service
   *
   * @param data: Content to post
   *
   * @param userId: User id to authenticate with.  If not needed, then it need not be given or be an empty string.
   *
   * @param password: Password to authenticate with.  If not needed, then it need not be given or be an empty string.
   *
   * @param challengeScheme: Defaults to ChallengeScheme.HTTP_BASIC.
   *
   * @param trustKnownCertificates: If true, select the list of certificates specified with the <code>TrustKnownCertificates.init</code>
   * function. Defaults to false.
   */
  def httpsPostMulipartFormX(
    url: String,
    file: File,
    userId: String = "",
    password: String = "",
    challengeScheme: ChallengeScheme = ChallengeScheme.HTTP_BASIC,
    trustKnownCertificates: Boolean = false,
    parameterList: Map[String, String] = Map()): Either[ResourceException, Representation] = {
    val clientResource = getClientResource(url, trustKnownCertificates, parameterList)
    val challengeResponse = new ChallengeResponse(challengeScheme, userId, password)
    clientResource.setChallengeResponse(challengeResponse)
    try {
      //      val data = Array[Byte]()
      //      val jjjjj = new FileRepresentation(data, MediaType.MULTIPART_FORM_DATA)
      val representation = new FileRepresentation(file, MediaType.MULTIPART_FORM_DATA)
      // val representation = new FileRepresentation(file, MediaType.APPLICATION_ALL)
      //val representation = new FileRepresentation(file, MediaType.MULTIPART_FORM_DATA)
      Right(clientResource.post(representation))
    } catch {
      case re: ResourceException => {
        Left(re)
      }
    }
  }

  /**
   * Post a files as a form set data.
   *
   * @param url post to here
   *
   * @param file Post this file
   *
   * @param fileMediaType Media type (zip, xml, etc).
   *
   * @param userId: User id to authenticate with.  If not needed, then it need not be given or be an empty string.
   *
   * @param password: Password to authenticate with.  If not needed, then it need not be given or be an empty string.
   *
   * @param challengeScheme: Defaults to ChallengeScheme.HTTP_BASIC.
   *
   * @param trustKnownCertificates: If true, select the list of certificates specified with the <code>TrustKnownCertificates.init</code>
   * function. Defaults to false.
   */
  def httpsPostSingleFileAsMulipartForm(
    url: String,
    file: File,
    fileMediaType: MediaType,
    userId: String = "",
    password: String = "",
    challengeScheme: ChallengeScheme = ChallengeScheme.HTTP_BASIC,
    trustKnownCertificates: Boolean = false,
    parameterList: Map[String, String] = Map()): Either[ResourceException, Representation] = {
    try {

      val entity = new FileRepresentation(file, fileMediaType) //create the fileRepresentation

      val fds = new FormDataSet
      fds.getEntries.add(new FormData("FileTag", entity))
      fds.setMultipart(true)

      val clientResource = getClientResource(url, trustKnownCertificates, parameterList)

      val challengeResponse = new ChallengeResponse(challengeScheme, userId, password)
      clientResource.setChallengeResponse(challengeResponse)
      //val representation = clientResource.post(fds, MediaType.MULTIPART_FORM_DATA)
      val representation = clientResource.post(fds, MediaType.MULTIPART_FORM_DATA)
      Right(representation)
    } catch {
      case re: ResourceException => {
        Left(re)
      }
    }
  }

  /**
   * Post multiple files as form set data.
   *
   * @param url post to here
   *
   * @param fileList Post these files in this order
   *
   * @param fileMediaType Media type (same for all files).  If the files do not have all the same media type, then this function will not work.
   *
   * @param userId: User id to authenticate with.  If not needed, then it need not be given or be an empty string.
   *
   * @param password: Password to authenticate with.  If not needed, then it need not be given or be an empty string.
   *
   * @param challengeScheme: Defaults to ChallengeScheme.HTTP_BASIC.
   *
   * @param trustKnownCertificates: If true, select the list of certificates specified with the <code>TrustKnownCertificates.init</code>
   * function. Defaults to false.
   */
  def httpsPostMultipleFilesAsMulipartForm(
    url: String,
    fileList: Seq[File],
    fileMediaType: MediaType,
    userId: String = "",
    password: String = "",
    challengeScheme: ChallengeScheme = ChallengeScheme.HTTP_BASIC,
    trustKnownCertificates: Boolean = false,
    parameterList: Map[String, String] = Map()): Either[ResourceException, Representation] = {
    try {

      var inc = 1
      val fds = new FormDataSet
      for (file <- fileList) {
        val entity = new FileRepresentation(file, fileMediaType) //create the fileRepresentation
        fds.getEntries.add(new FormData("FileTag" + inc, entity))
        inc = inc + 1
        fds.setMultipart(true)
      }

      val clientResource = getClientResource(url, trustKnownCertificates, parameterList)
      val challengeResponse = new ChallengeResponse(challengeScheme, userId, password)
      clientResource.setChallengeResponse(challengeResponse)
      val representation = clientResource.post(fds, MediaType.MULTIPART_FORM_DATA)
      Right(representation)
    } catch {
      case re: ResourceException => {
        Left(re)
      }
    }
  }

  private def main3(args: Array[String]): Unit = { // TODO rm
    val file1 = new File("""D:\tmp\aqa\CBCT\MQATX1OBIQA2019Q3\tiny.txt""")
    val file2 = new File("""D:\tmp\aqa\CBCT\MQATX1OBIQA2019Q3\TX1_2019_05_14_upload.zip""")
    val file3 = new File("""D:\tmp\aqa\CBCT\MQATX1OBIQA2019Q3\TX1_2019_05_14_a.zip""")
    val data = FileUtil.readBinaryFile(file1).right.get
    //val status = httpsPostSingleFileAsMulipartForm("http://localhost/run/BBbyCBCT_6", file1, MediaType.APPLICATION_ALL, "userid", "password")
    //val status = httpsPostSingleFileAsMulipartForm("http://localhost/run/BBbyCBCT_6", file1, MediaType.APPLICATION_ZIP, "userid", "password")
    val status = httpsPostSingleFileAsMulipartForm("http://localhost/run/BBbyCBCT_6?Run=Run", file3, MediaType.APPLICATION_ZIP, "userid", "password")
    status match {
      case Left(ex) => println("Resource exception: " + ex)
      case Right(rep) => {
        println("Representation: " + rep)
        val baos = new java.io.ByteArrayOutputStream
        FileUtil.copyStream(rep.getStream, baos)
        val text = new String(baos.toByteArray)
        println("Return text: " + text)
      }
    }
    println("status: " + status)
  }

  private def main2(args: Array[String]): Unit = { // TODO rm
    val file1 = new File("""D:\tmp\aqa\CBCT\MQATX1OBIQA2019Q3\TX1_2019_05_14_upload.zip""")
    val file2 = new File("""D:\tmp\aqa\CBCT\MQATX1OBIQA2019Q3\tiny.txt""")
    val fileList = Seq(file1)
    //val status = httpsPost("http://localhost/run/BBbyCBCT_6", data, "userid", "password")
    //val status = httpsPostZipFile("http://localhost/run/BBbyCBCT_6", file, "userid", "password")
    val status = httpsPostMultipleFilesAsMulipartForm("http://localhost/run/BBbyCBCT_6/?Runny=yeah", fileList, MediaType.TEXT_PLAIN, "userid", "password")
    println("status: " + status)
  }

  def main(args: Array[String]): Unit = { // TODO rm

    val fileList = new File("""D:\pf\eclipse\workspaceOxygen\aqaclient\src\main\resources\static\certificates""").listFiles
    TrustKnownCertificates.init(fileList)

    val url = "https://uhroappwebsdv1.umhs.med.umich.edu:8111/GetSeries?PatientID=MQATX4OBIQA2019Q3"
    val status = httpsGet(url, "irrer", "45eetslp", ChallengeScheme.HTTP_BASIC, true)
    Trace.trace("status: " + status)
    if (status.isRight) {
      val rep = status.right.get
      Trace.trace(rep)
      Trace.trace(rep.getText)
    }
  }

}
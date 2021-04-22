package edu.umro.RestletUtil

import edu.umro.ScalaUtil.FileUtil
import org.restlet.engine.ssl.DefaultSslContextFactory

import java.io.{ByteArrayInputStream, File, FileInputStream, InputStream}
import java.security.cert.{Certificate, CertificateException, CertificateFactory, X509Certificate}
import javax.net.ssl.{SSLContext, TrustManager, X509TrustManager}

/**
  * Support secure communications via a java HTTPS client by setting up a
  * list of known certificates that are to be trusted.  This helps in
  * simplifying the deployment of software.  The alternate way to set up
  * certificates is to add them to the local system's java key store file,
  * but determining where that is can be ambiguous and what the password
  * is can be problematic.
  *
  * To use this, call the <code>TrustKnownCertificates.init(fileList: Seq[File])</code>
  * function with a list of certificate files, and then when calling the HttpsClient.httpsGet
  * function specify the <code>trustKnownCertificates</code> as <code>true</code>.
  */
class TrustKnownCertificates extends X509TrustManager {

  override def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {}

  /**
    * Throw CertificateException if any member of the incoming certificate chain is not trusted.
    *
    * If the known certificate list is empty, and the caller is requesting this check, then trust
    * all servers.  In general, trusting all servers is a bad practice (because it can allow man
    * in the middle attacks), but in special cases (such as talking to a server that is local to
    * the machine) it can be ok.
    */
  override def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = {

    val knownCertList = TrustKnownCertificates.knownCerts

    if (knownCertList.nonEmpty) {
      def certTrusted(cert: X509Certificate) = {
        val encoded = cert.getEncoded
        val ok = knownCertList.exists(c => java.util.Arrays.equals(c.getEncoded, encoded))
        ok
      }
      val trust = certTrusted(chain.head)

      if (!trust)
        throw new CertificateException("Untrusted certificate (first in chain):\n\n" + chain.head.toString)
    }
  }

  override def getAcceptedIssuers: Array[X509Certificate] = TrustKnownCertificates.knownCerts
}

class TrustingSslContextFactory extends DefaultSslContextFactory {
  override def createSslContext: SSLContext = {
    val sslContext = SSLContext.getInstance("TLS")
    val tm = Array[TrustManager](new TrustKnownCertificates)
    sslContext.init(null, tm, null)
    createWrapper(sslContext)
  }
}

object TrustKnownCertificates {

  private val endCert = "-----END CERTIFICATE-----"

  val certFactory: CertificateFactory = CertificateFactory.getInstance("X.509")

  private def certFileToInputStreamList(file: File): Seq[InputStream] = {

    def binToStreams(data: Array[Byte]) = {
      val text = new String(data)
      if (text.contains(endCert))
        text.trim.split(endCert).map(t => new ByteArrayInputStream((t + endCert).getBytes)).toSeq
      else
        Seq(new FileInputStream(file))
    }

    FileUtil.readBinaryFile(file) match {
      case Right(data) => binToStreams(data)
      case _           => Seq[InputStream]()
    }
  }

  /**
    * mutable local place to keep certificates
    */
  private val configuredCertsBuffer = scala.collection.mutable.ArrayBuffer[X509Certificate]()

  /**
    * Provide read-only access to configured certs.
    */
  def knownCerts: Array[X509Certificate] = configuredCertsBuffer.toList.toArray

  private def inStreamToCert(inStream: InputStream): Option[Certificate] = {
    try {
      Some(certFactory.generateCertificate(inStream))
    } catch {
      case _: Throwable => None
    }
  }

  def init(fileList: Seq[File]): Unit = {
    configuredCertsBuffer.clear
    //fileList.map(f => readCert(f)).flatten.groupBy(_.getPublicKey.toString).map(c => c._2.head.asInstanceOf[X509Certificate])
    fileList
      .flatMap(f => certFileToInputStreamList(f))
      .flatMap(inStream => inStreamToCert(inStream))
      . // convert each stream to certificate, discard the bad ones
      groupBy(_.getPublicKey.toString)
      .map(c => c._2.head.asInstanceOf[X509Certificate])
      . // find distinct and take one of each
      foreach(cert => configuredCertsBuffer.append(cert)) // save in list
    println(configuredCertsBuffer.mkString("\n=====================================================\n"))
  }

}

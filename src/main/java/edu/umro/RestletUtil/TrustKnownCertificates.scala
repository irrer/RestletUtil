package edu.umro.RestletUtil

import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import org.restlet.engine.ssl.DefaultSslContextFactory
import javax.net.ssl.TrustManager
import java.security.cert.X509Certificate
import edu.umro.ScalaUtil.Trace
import java.io.File
import java.io.InputStream
import edu.umro.ScalaUtil.FileUtil
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.Certificate
import java.io.FileInputStream
import java.security.cert.CertificateException

class TrustKnownCertificates extends X509TrustManager {

  private val noCerts = Array[X509Certificate]()

  override def checkClientTrusted(chain: Array[X509Certificate], authType: String) = {}

  /**
   * Throw CertificateException if any member of the incoming certificate chain is not trusted.
   */
  override def checkServerTrusted(chain: Array[X509Certificate], authType: String) = {

    def certTrusted(cert: X509Certificate) = {
      val encoded = cert.getEncoded
      val ok = TrustKnownCertificates.configuredCerts.find(c => java.util.Arrays.equals(c.getEncoded, encoded)).isDefined
      ok
    }

    val trustStatusList = chain.map(cert => (certTrusted(cert), cert))
    val trustEntireChain = trustStatusList.map(tc => tc._1).reduce(_ && _)

    if (!trustEntireChain)
      throw new CertificateException("Untrusted certificate(s) found: " + trustStatusList.filterNot(_._1).map(tc => tc._2.toString).mkString("\n\n"))
  }

  override def getAcceptedIssuers = TrustKnownCertificates.configuredCerts
}

class TrustingSslContextFactory extends DefaultSslContextFactory {
  override def createSslContext: SSLContext = {
    val sslContext = SSLContext.getInstance("TLS");
    val tm = Array[TrustManager](new TrustKnownCertificates)
    sslContext.init(null, tm, null);
    createWrapper(sslContext);
  }
}

object TrustKnownCertificates {

  private val endCert = "-----END CERTIFICATE-----"

  val certFactory = CertificateFactory.getInstance("X.509")

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
      case _ => Seq[InputStream]()
    }
  }

  private def readCert(file: File): Option[Certificate] = {
    try {
      val fis = new FileInputStream(file)
      val cert = certFactory.generateCertificate(fis) //.asInstanceOf[X509Certificate]
      Some(cert)
    } catch {
      case t: Throwable => None
    }
  }

  /**
   * mutable local place to keep certificates
   */
  private val configuredCertsBuffer = scala.collection.mutable.ArrayBuffer[X509Certificate]()

  /**
   * Provide read-only access to configured certs.
   */
  def configuredCerts = configuredCertsBuffer.toList.toArray

  private def inStreamToCert(inStream: InputStream): Option[Certificate] = {
    try {
      Some(certFactory.generateCertificate(inStream))
    } catch {
      case t: Throwable => None
    }
  }

  def init(fileList: Seq[File]) = {
    configuredCertsBuffer.clear
    //fileList.map(f => readCert(f)).flatten.groupBy(_.getPublicKey.toString).map(c => c._2.head.asInstanceOf[X509Certificate])
    val j = fileList.map(f => certFileToInputStreamList(f)).flatten. // get readable content from files, discard the bad ones
      map(inStream => inStreamToCert(inStream)).flatten. // convert each stream to certificate, discard the bad ones
      groupBy(_.getPublicKey.toString).map(c => c._2.head.asInstanceOf[X509Certificate]). // find distinct and take one of each
      map(cert => configuredCertsBuffer.append(cert.asInstanceOf[X509Certificate])) // save in list
  }

  val fileList = new File("""D:\pf\eclipse\workspaceOxygen\aqaclient\src\main\resources\static\certificates""").listFiles
  init(fileList)

}
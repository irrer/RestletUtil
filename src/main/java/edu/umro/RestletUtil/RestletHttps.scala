package edu.umro.RestletUtil

import org.restlet.Component
import org.restlet.data.Protocol
import java.io.File
import java.security.KeyStore
import java.io.FileInputStream
import scala.Left
import scala.Right
import resource.managed

/**
 * Support for setting up HTTPS for Restlet using java keystore (*.jks) file.
 */
object RestletHttps {

    private def fileOfProperty(key: String): Option[File] = {
        val fileName = System.getProperties.getProperty(key)
        if (fileName != null) {
            val file = new File(fileName)
            if (file.canRead) Some(file)
            else None
        }
        else None
    }

    private def standardKeystoreFileNameList: List[String] = {
        val javaHome = System.getenv("JAVA_HOME")
        if (javaHome != null) {
            val stdNameList = List("/lib/security/jssecacerts", "/lib/security/cacerts")
            stdNameList.map(name => javaHome + (name.replace("/", File.separator)))
        }
        else List[String]()
    }

    private def umroKeystoreFileNameList: List[String] = {
        List(
            "C:\\Program Files\\UMRO\\keystore.jks",
            "C:\\Program Files\\UMRO\\keystore.p12")
    }

    private def propertiesKeystoreFileNameList: List[String] = {
        val keyList = List("javax.net.ssl.keyStore", "javax.net.ssl.trustStore")
        keyList.map(key => System.getProperties.getProperty(key)).filter(name => name != null)
    }

    def standardKeystoreFileList: List[File] = {
        (umroKeystoreFileNameList ++ standardKeystoreFileNameList ++ propertiesKeystoreFileNameList).map(name => new File(name)).filter(file => file.canRead())
    }

    def standardPasswordList: List[String] = {
        val keyList = List("javax.net.ssl.keyStorePassword", "javax.net.ssl.trustStorePassword")
        keyList.map(key => System.getProperties.getProperty(key)).filter(pw => pw != null)
    }

    class KeystorePassword(val keyStoreFile: File, val password: String, val keystoreType: String) {
        /**
         * Determine whether or not the password works with the keystore file.
         *
         * @return True on success.
         */
        def getKeyStore: Either[String, KeyStore] = {
            managed(new FileInputStream(keyStoreFile)) acquireAndGet {
                fileInputStream =>
                    {
                        try {
                            val ks = KeyStore.getInstance(keystoreType)
                            ks.load(fileInputStream, password.toCharArray);
                            Right(ks)
                        }

                        catch {
                            case e: Exception => {
                                Left(e.getMessage)
                            }
                        }
                    }
            }
        }

        def keyListToString: String = {
            val text = new StringBuffer("aliasList: ")
            try {
                val keyStore = getKeyStore.right.get
                val aliasList = keyStore.aliases
                val protection = new KeyStore.PasswordProtection(password.toCharArray)
                while (aliasList.hasMoreElements) {
                    val alias = aliasList.nextElement
                    val t = "\n    alias: " + alias + " : " + keyStore.getEntry(alias, protection)
                    text.append(t)
                    //text.append("\n    alias: " + alias + " : " + keyStore.getEntry(alias, null))
                }
            }
            catch {
                case e: Exception => ;
            }
            text.toString
        }

        override def toString: String = {
            val lengthToShow = if (password.length > 16) 4 else (password.length / 4)
            "file: " + keyStoreFile.getAbsolutePath + "    type: " + keystoreType + "    password begins with: " + password.subSequence(0, lengthToShow)
        }
    }

    private def findKeystore(fileList: List[File], passwordList: List[String]): Option[KeystorePassword] = {
        if (fileList.isEmpty) None
        else {
            val keyStoreTypeList = List(KeyStore.getDefaultType, "jks", "pkcs12").distinct
            val kpList = for (pw <- passwordList; file <- fileList; keyStoreType <- keyStoreTypeList) yield new KeystorePassword(file, pw, keyStoreType)
            kpList.find { kp => kp.getKeyStore.isRight }
        }
    }

    private def setup(component: Component, httpsPort: Int, fileList: List[File], passwordList: List[String]): Either[String, KeystorePassword] = {
        val ks = findKeystore(fileList, passwordList)
        if (ks.isDefined) {
            val server = component.getServers().add(Protocol.HTTPS, httpsPort)
            val parameters = server.getContext().getParameters()
            parameters.add("sslContextFactory", "org.restlet.engine.ssl.DefaultSslContextFactory")
            parameters.add("keyStorePath", ks.get.keyStoreFile.getAbsolutePath)
            parameters.add("keyStorePassword", ks.get.password)
            parameters.add("keyPassword", ks.get.password)
            parameters.add("keyStoreType", ks.get.keystoreType)
            Right(ks.get)
        }
        else Left("Could not find keystore/password combination in this list of files:" + (fileList.foldLeft("")((t, f) => t + "\n    " + f.getAbsolutePath)))
    }

    /**
     * Add the HTTPS protocol to the Restlet component.
     * 
     * Note that this requires that the keystore password is the same as the key password.
     *
     * @param fileList List of possible keystore files.  They are tried in the order given with
     * each of the passwords given, and the first one that successfully loads is used.  If this
     * list is empty then only standard locations are searched.  The standard locations include
     * references made by the javax.net.ssl.keyStore and javax.net.ssl.trustStore System properties.
     *
     * @param passwordList : List of possible keystore passwords.  If this list is empty then only
     * standard locations are tried.  Standard locations include the passwords defined by the
     * javax.net.ssl.keyStorePassword and javax.net.ssl.trustStorePassword System properties.
     *
     * @return The keystore/password combination that is being used, or a message (that does not contain passwords
     * so it is safe for logging) describing the problem.
     */
    def addHttps(component: Component, httpPort: Int, fileList: List[File], passwordList: List[String]): Either[String, KeystorePassword] = {
        // Add system standard keystore file locations
        val fList = (fileList ++ standardKeystoreFileList).distinct.filter(f => f.canRead()) // cull list by removing duplicates and require readability

        val pwList = passwordList ++ standardPasswordList // Add system standard password sources

        0 match {
            case _ if (fList.isEmpty) => Left("No files found to use for keystore")
            case _ if (pwList.isEmpty) => Left("No passwords given to use for keystore")
            case _ => setup(component, httpPort, fList, pwList)
        }

    }

}

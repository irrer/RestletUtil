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

/**
 * For testing the Https object.  Note that these need to be top-level classes so
 * that Scala will not make class names that Restlet can not handle.
 */

class HttpsTesting extends Restlet {
    override def handle(request: Request, response: Response): Unit = {
        response.setStatus(Status.SUCCESS_OK)
        val msg = "Yay - it works! " + (new Date)
        response.setEntity(msg, MediaType.TEXT_PLAIN)
        println(msg)
    }

}

class HttpsTestApp extends Application {

    val httpsPort = 9099

    val component = new Component
    val fileNameList = List(
        "keystore.jks",
        "C:\\Program Files\\UMRO\\keystore.jks",
        "src\\main\\resources\\keystore.jks",
        "C:\\Program Files\\UMRO\\keystore.jks")

    val pwList = List("aFakePassword", "", "thisWillNotWork")

    val ks = RestletHttps.addHttps(component, httpsPort, fileNameList.map(fn => new File(fn)), pwList)

    if (ks.isLeft) {
        println("Failed: " + ks.left.get)
        System.exit(1)
    }
    else println("Success setting up keystore: " + ks.right.get)

    val httpsTesting = new HttpsTesting

    override def createInboundRoot(): Restlet = {
        val router = new Router(getContext.createChildContext)
        router.setDefaultMatchingMode(Template.MODE_STARTS_WITH)
        router.attach("/", httpsTesting)
        router
    }
}

object HttpsTesting {

    /**
     * For testing and development only.
     */
    def main(args: Array[String]): Unit = {
        println("Starting")

        val httpsPort = 9099

        println("Standard files: ")
        RestletHttps.standardKeystoreFileList.map(f => println("    " + f.getAbsolutePath))
        println("Number of standard passwords found: " + RestletHttps.standardPasswordList.size)
        val app = new HttpsTestApp

        app.component.getDefaultHost.attach(app)
        println("starting web service ...")
        app.component.start

        print("Waiting.  Go to \n\n    https://localhost:" + httpsPort + "\n\nand ignore scary warnings.\n\nCountdown until automatic shutdown:")
        val stop = System.currentTimeMillis + (10 * 60 * 1000)
        while (System.currentTimeMillis < stop) {
            Thread.sleep(1000)
            val remaining = (stop - System.currentTimeMillis) / 1000
            print(" " + remaining)
            if ((remaining % 50) == 0) println
        }
        println("\nExiting...")
        System.exit(0)
    }

}
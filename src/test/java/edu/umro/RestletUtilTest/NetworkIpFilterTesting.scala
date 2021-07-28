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
import org.restlet.data.Protocol
import org.restlet.Restlet
import org.restlet.Component
import java.util.Date
import edu.umro.RestletUtil.NetworkIpFilter

/**
 * For testing the NetworkIpFilter.
 */

private class Trivial extends Restlet {
    override def handle(request: Request, response: Response): Unit = {
        response.setStatus(Status.SUCCESS_OK)
        val msg = "This request was allowed through from " + request.getClientInfo.getAddress + "    " + (new Date)
        response.setEntity(msg, MediaType.TEXT_PLAIN)
        println(msg)
    }

}

private class NetworkIpFilterTestApp extends Application {
    override def createInboundRoot: Restlet = {
        val networkFilter = new NetworkIpFilter(getContext, NetworkIpFilterTesting.allowedIpList)
        networkFilter.setNext(new Trivial)
        networkFilter
    }
}

object NetworkIpFilterTesting {
    val httpPort = 9098
    val allowedIpList = List("127.0.0.2", "141.214.124.240")

    private def waitAndExit = {
        print("Waiting.  Go to \n\n    http://localhost:" + httpPort + "\n\nCountdown until automatic shutdown:")
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

    /**
     * For testing and development only.
     */
    def main(args: Array[String]): Unit = {
        println("Starting")

        println("allowed IP addresses: ")
        allowedIpList.map(ip => println("\n    " + ip))

        val app = new NetworkIpFilterTestApp
        val component = new Component
        component.getServers().add(Protocol.HTTP, httpPort)
        component.getDefaultHost.attach(app);

        component.start

        waitAndExit
    }

}
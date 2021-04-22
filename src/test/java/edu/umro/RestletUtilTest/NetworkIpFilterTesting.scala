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
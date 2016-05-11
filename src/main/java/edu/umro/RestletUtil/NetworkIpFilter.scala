package edu.umro.RestletUtil

import org.restlet.routing.Filter
import org.restlet.Request
import org.restlet.Response
import org.restlet.data.MediaType
import org.restlet.data.Status
import org.restlet.Context
import org.restlet.data.Protocol
import scala.xml.PrettyPrinter

/**
 * Only allow clients with the configured IP addresses access.
 */
class NetworkIpFilter(context: Context, AllowedHttpIpList: List[String]) extends Filter(context) {

    private val regExList: List[String] = {
        AllowedHttpIpList.map(a => a.replace('.', ' ').replace("*", ".*"))
    }

    private def allow(ipAddress: String): Boolean = {
        if (ipAddress == null) false
        else {
            val clientIp = ipAddress.replace('.', ' ')
            regExList.filter(regEx => clientIp.matches(regEx)).size > 0
        }
    }

    val forbidden: String = {
        val message = {
            <html>
                <head>
                    <title>Forbidden</title>
                </head>
                <body>
                    <center>
                        <h2>
                            <p/><br/><p/><br/>
                            You are not authorized to access this web site.
                            <p/>
                        </h2>
                    </center>
                </body>
            </html>
        }
        new PrettyPrinter(1024, 2).format(message)
    }

    override def beforeHandle(request: Request, response: Response): Int = {
        if (allow(request.getClientInfo.getAddress)) {
            Filter.CONTINUE
        }
        else {
            getLogger.warning("Incoming HTTP request from " + request.getClientInfo.getAddress + " denied due to IP address filtering.")
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN)
            response.setEntity(forbidden, MediaType.TEXT_HTML)
            Filter.STOP
        }
    }
}

object NetworkIpFilter;
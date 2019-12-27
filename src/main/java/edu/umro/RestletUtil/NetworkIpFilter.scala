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

    /**
     * List of regular expressions that IP address should match.  Separating '.' for IPv4 or ':' for IPv6.
     */
    private val regExList: List[String] = {
        AllowedHttpIpList.map(a => a.replace('.', ' ').replace(':', ' ').replace("*", ".*"))
    }

    /**
     * Return true if the address is allowed.
     */
    private def allow(ipAddress: String): Boolean = {
        if (ipAddress == null) false
        else {
            val clientIp = ipAddress.replace('.', ' ').replace(':', ' ')
            regExList.find(regEx => clientIp.matches(regEx)).isDefined
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
                        <h2 style="margin-top:100px;">
                            You are not authorized to access this web site.
                        </h2>
                    </center>
                </body>
            </html>
        }
        "<!DOCTYPE html>\n" + new PrettyPrinter(1024, 2).format(message)
    }

    override def beforeHandle(request: Request, response: Response): Int = {

        if (allow(request.getClientInfo.getAddress)) {
            Filter.CONTINUE
        }
        else {
            val attrListText = request.getAttributes.toString.replaceAll(", ", ",\n        ")
            val clientInfo = request.getClientInfo

            val msg =
                "Incoming HTTP request from " + request.getClientInfo.getAddress + " denied due to IP address filtering.\n" +
                    "    HTTP Method: " + request.getMethod + "\n" +
                    "    Original Ref: " + request.getOriginalRef + "\n" +
                    "    Client Port: " + clientInfo.getPort + "\n" +
                    "    Client Accepted CharacterSets: " + clientInfo.getAcceptedCharacterSets + "\n" +
                    "    Client Accepted Languages: " + clientInfo.getAcceptedLanguages + "\n" +
                    "    Attributes:\n        " + attrListText + "\n"
            getLogger.warning(msg)
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN)
            response.setEntity(forbidden, MediaType.TEXT_HTML)
            Filter.STOP
        }
    }
}

object NetworkIpFilter;

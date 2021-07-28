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

import org.restlet.data.{MediaType, Status}
import org.restlet.routing.Filter
import org.restlet.{Context, Request, Response}

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
      regExList.exists(regEx => clientIp.matches(regEx))
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
    } else {
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

package edu.umro.RestletUtil

import org.restlet.Request
import org.restlet.Response
import org.restlet.data.MediaType
import org.restlet.data.Status
import org.restlet.Context
import org.restlet.data.Protocol
import scala.xml.PrettyPrinter
import java.util.Date
import org.restlet.routing.Filter
import org.restlet.Restlet

/**
 * Return contents with an expiration date of sometime in the future.
 *
 * @param interval: Number of milliseconds in the future to expire content.
 *
 * @param context: Restlet context (use this.getContext)
 *
 * @param wrapped: If provided, wrap with this filter so that the content it provides is effected.
 */
class ExpiresLaterFilter(context: Context, interval: Long, wrapped: Option[Restlet]) extends Filter(context) {

    def this(context: Context, interval: Long) = this(context, interval, None)

    def this(context: Context, interval: Long, wrapped: Restlet) = this(context, interval, Some(wrapped))

    if (wrapped.isDefined) setNext(wrapped.get)

    override def afterHandle(request: Request, response: Response): Unit = {
        if (response.getEntity != null) {
            val expirationDate = new Date(System.currentTimeMillis + interval)
            response.getEntity.setExpirationDate(expirationDate)
        }
    }
}

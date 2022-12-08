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

import org.restlet.routing.Filter
import org.restlet.{Context, Request, Response, Restlet}

import java.util.Date

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

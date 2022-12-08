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

import org.restlet.security.{SecretVerifier, Verifier}

import java.text.SimpleDateFormat
import java.util.Date

/**
  * Cache secrets to reduce costly verifications.
  *
  * @param actualVerify : Function that accepts the user ID and secret (password) and
  * returns true if authorized, false if not.
  *
  * @param expirationIntervalInMs : Time in milliseconds before users authorization will
  * expire.  After this, the actualVerify will be called again to renew them.
  */
class CachedSecretVerifier(actualVerify: (String, Array[Char]) => Boolean, expirationIntervalInMs: Long) extends SecretVerifier {

  /** List of verified users and their expiration time. */
  private val history = collection.mutable.Map[String, Long]()

  private def add(userId: String) = history.put(userId, System.currentTimeMillis + expirationIntervalInMs)

  private def clean = {
    val now = System.currentTimeMillis
    val old = history.filter(h => h._2 < now)
    old.map(h => history.remove(h._1))
  }

  private def doVerify(userId: String, secret: Array[Char]): Int = {
    if (actualVerify(userId, secret)) {
      add(userId)
      Verifier.RESULT_VALID
    } else Verifier.RESULT_INVALID
  }

  /**
    * Get a list of active users with their expiration times.
    */
  def getActiveUsers: List[(String, Long)] = history.synchronized(history.toList)

  override def toString: String = {
    val timeFormat = new SimpleDateFormat("HH:mm:ss")
    def fmtTime(t: Long): String = timeFormat.format(new Date(t - System.currentTimeMillis))
    def fmtHistory(hist: (String, Long)): String = hist._1.formatted("%16s") + " : " + fmtTime(hist._2)
    history.foldLeft("Verified users an time until expiration:")((t, h) => t + ("\n    " + fmtHistory(h)))
  }

  /**
    * Remove any users whose logins have expired.
    */
  def removeExpiredEntries(): Unit = history.synchronized(clean)

  /**
    * Called by super class to validate user.  May be called multiple times for
    * a single HTTP request.
    */
  override def verify(userId: String, secret: Array[Char]): Int = {
    history.synchronized({
      clean
      if (history.contains(userId)) Verifier.RESULT_VALID
      else doVerify(userId, secret)
    })
  }

}

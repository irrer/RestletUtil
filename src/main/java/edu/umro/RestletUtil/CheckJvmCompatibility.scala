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

import edu.umro.ScalaUtil.{Logging, Trace}

/**
  * Test and provide examples for HttpsClient.
  */
object CheckJvmCompatibility extends Logging {

  def main(args: Array[String]): Unit = {

    println("Starting ...")

    try {
      Trace.trace()
      Trace.trace("java.version: " + System.getProperty("java.version"))
      Trace.trace("getImplementationVersion: " + Runtime.getRuntime.getClass.getPackage.getImplementationVersion)
      val jInt = 5
      val j2 = jInt.formatted("%05d")
      Trace.trace(j2)
      val j1 = "hey".formatted("%5s")
      Trace.trace(j1)
      val j = org.slf4j.Logger.ROOT_LOGGER_NAME
      Trace.trace(j)
      Trace.trace("Success!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    } catch {
      case t: Throwable =>
        Trace.trace(t)
        Trace.trace(fmtEx(t))
        Trace.trace("Failure -----")
    }
    println("Done. Exiting with status 99 ...")
    System.exit(99)

  }

}

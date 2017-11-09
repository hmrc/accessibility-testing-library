/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.accessibility.audit

import java.security.MessageDigest
import java.util.logging.Logger

import cucumber.api.Scenario
import org.openqa.selenium.{JavascriptExecutor, WebDriver}

object AccessibilityTester extends AuditRunner {
  val logger = Logger.getLogger(AccessibilityTester.getClass.getName)
  var tester: AccessibilityTester = _
  private var compatibleDriver = false

  def initialise(driver: WebDriver, testerConstructor: (WebDriver with JavascriptExecutor => AccessibilityTester) = new AccessibilityTester(_)): Boolean = {
    compatibleDriver = false
    try {
      driver.asInstanceOf[JavascriptExecutor] // Forces cast to the see if type actually matches
      val executor = driver.asInstanceOf[WebDriver with JavascriptExecutor]
      tester = testerConstructor(executor)
      compatibleDriver = true
      logger.info("Current driver class can execute JavaScript so accessibility is enabled.")
    } catch {
      case ex: ClassCastException => {
        logger.warning(
          s"""Current driver class '${driver.getClass.getName}' cannot be cast to JavascriptExecutor so accessibility
             | testing will not return results.""".stripMargin)
      }
    }
    compatibleDriver
  }

  def startScenario(scenario: Scenario): Unit = {
    if (compatibleDriver) {
      tester.startScenario(scenario)
    }
  }

  def endScenario(): Unit = {
    if (compatibleDriver) {
      tester.endScenario()
    }
  }
}

class AccessibilityTester(driver: WebDriver with JavascriptExecutor,
                          codeSnifferConstructor: (WebDriver with JavascriptExecutor => CodeSniffer) = new CodeSniffer(_)) {
  private lazy val digest = MessageDigest.getInstance("SHA1")
  val logger = Logger.getLogger(AccessibilityTester.getClass.getName)
  val sniffer: CodeSniffer = codeSnifferConstructor(driver)
  var currentScenario: Option[Scenario] = None
  var scenarioResults: Seq[AuditResult] = Seq.empty
  var cache: Map[String, Seq[AuditResult]] = Map.empty

  def startScenario(scenario: Scenario): Unit = {
    currentScenario = Some(scenario)
    scenarioResults = Seq.empty
  }

  def endScenario(): Unit = {
    currentScenario match {
      case Some(s) => {
        s.write(AuditReport.makeScenarioSummary(scenarioResults))
        scenarioResults = Seq.empty
        currentScenario = None
      }
      case None => {
        logger.severe("endScenario has been called without startScenario so behaviour is undefined.")
      }
    }
  }

  def checkContent(filter: AuditResult => Boolean = AuditFilters.emptyFilter): Unit = {
    currentScenario match {
      case Some(s) => {
        val hash = hashcodeOf(driver.getPageSource)
        val data = cache.getOrElse(hash, {
          val results = sniffer.run()
          cache = cache + (hash -> results)
          results
        }).filter(filter)
        if (data.nonEmpty) {
          scenarioResults ++= data
          s.write(s"<h3>Found ${data.size} issues on ${driver.getTitle} (${driver.getCurrentUrl})</h3>\n${AuditReport.makeTable(data)}")
        }
      }
      case None => {
        logger.severe("checkContent has been called without startScenario so no checking will be done.")
      }
    }
  }

  private def hashcodeOf(source: String): String = {
    digest.digest(source.getBytes()).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }

}

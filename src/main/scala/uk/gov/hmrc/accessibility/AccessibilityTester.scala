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

package uk.gov.hmrc.accessibility

import java.security.MessageDigest
import java.util.logging.Logger
import cucumber.api.Scenario
import org.openqa.selenium.{JavascriptExecutor, WebDriver}

object AccessibilityTester {
  private var scenario: Scenario = _
  private var scenarioResults : Seq[AccessibilityResult] = Seq.empty

  private var cache : Map[String,Seq[AccessibilityResult]] = Map.empty
  private lazy val digest = MessageDigest.getInstance("SHA1")
  private def hashcodeOf (source: String) : String = {
    digest.digest(source.getBytes()).map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
  }

  def startScenario(scenario: Scenario): Unit = {
    this.scenario = scenario
    scenarioResults = Seq.empty
  }

  def endScenario(): Unit = {
    scenario.write(AccessibilityReport.makeScenarioSummary(scenarioResults))
  }

  def checkContent(driver : WebDriver, filter : AccessibilityResult => Boolean = AccessibilityFilters.emptyFilter) : Unit = {
    try {
      val executor = driver.asInstanceOf[WebDriver with JavascriptExecutor]
      val hash = hashcodeOf(executor.getPageSource)

      val data = cache.getOrElse(hash, {
        val results = new CodeSniffer(executor).run().filter(filter)
        cache = cache + (hash -> results)
        results
      })

      if (data.nonEmpty) {
        scenarioResults ++= data
        scenario.write(s"<h3>Found ${data.size} issues on ${driver.getTitle} (${driver.getCurrentUrl})</h3>\n${AccessibilityReport.makeTable(data)}")
      }
    } catch {
      case ex : ClassCastException => Logger.getLogger(AccessibilityTester.getClass.getName).warning(
        s"""
           |Current driver class '${driver.getClass.getName}' cannot be cast to JavascriptExecutor so accessibility
           | testing will not return results.
           |""".stripMargin)
    }
  }

}

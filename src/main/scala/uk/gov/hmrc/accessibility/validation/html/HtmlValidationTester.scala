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

package uk.gov.hmrc.accessibility.validation.html

import java.util.logging.Logger

import cucumber.api.Scenario
import org.openqa.selenium.WebDriver
import uk.gov.hmrc.accessibility.CucumberIntegration
import uk.gov.hmrc.accessibility.validation.ValidationRunner

object HtmlValidationTester {
  def initialise(driver: WebDriver, runner: ValidationRunner = new ProcessHtmlValidationRunner): Option[HtmlValidationTester] = {
    Some(new HtmlValidationTester(driver, runner))
  }
}

class HtmlValidationTester(driver: WebDriver, runner: ValidationRunner = new ProcessHtmlValidationRunner) extends CucumberIntegration {
  val logger = Logger.getLogger(HtmlValidationTester.getClass.getName)
  var currentScenario: Option[Scenario] = None
  var scenarioResults: Seq[HtmlValidationError] = Seq.empty
  val htmlValidator = new HtmlValidator(runner)

  override def startScenario(scenario: Scenario): Unit = {
    currentScenario = Some(scenario)
    scenarioResults = Seq.empty
  }

  override def endScenario(): Unit = {
    currentScenario match {
      case Some(s) => {
        s.write(HtmlValidationReporter.makeSummary(scenarioResults))
        scenarioResults = Seq.empty
        currentScenario = None
      }
      case None => {
        logger.severe("endScenario has been called without startScenario so behaviour is undefined.")
      }
    }
  }

  def checkContent(): Unit = {
    currentScenario match {
      case Some(s) => {
        val results: Seq[HtmlValidationError] = htmlValidator.validate(driver.getPageSource)
        if (results.nonEmpty) {
          scenarioResults ++= results
          s.write(s"""<h3>Found ${results.size} issues on "${driver.getTitle}" (${driver.getCurrentUrl})</h3>\n${HtmlValidationReporter.makeTable(results)}""")
        }
      }
      case None => {
        logger.severe("checkContent has been called without startScenario so no checking will be done.")
      }
    }
  }
}

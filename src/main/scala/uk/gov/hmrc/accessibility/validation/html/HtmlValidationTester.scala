/*
 * Copyright 2018 HM Revenue & Customs
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

import cucumber.api.Scenario
import org.openqa.selenium.WebDriver
import uk.gov.hmrc.accessibility.{AccessibilityChecker, CachingChecker, CucumberAccessibilityTester}
import uk.gov.hmrc.accessibility.validation.ValidationRunner

object HtmlValidationTester {
  def initialise(driver: WebDriver, runner: ValidationRunner = new APIHtmlValidationRunner): Option[HtmlValidationTester] = {
    Some(new HtmlValidationTester(driver, runner))
  }
}

class HtmlValidationTester(driver: WebDriver, runner: ValidationRunner = new APIHtmlValidationRunner,
                           checkerCons: (ValidationRunner) => AccessibilityChecker[HtmlValidationError] =
                           (runner: ValidationRunner) => new CachingChecker[HtmlValidationError](new HtmlValidator(runner)))
  extends CucumberAccessibilityTester[HtmlValidationError] {
  val checker: AccessibilityChecker[HtmlValidationError] = checkerCons(runner)

  override def executeTest(pageSource: String): Seq[HtmlValidationError] = {
    checker.run(pageSource)
  }

  override def writeStepResults(scenario: Scenario, results: Seq[HtmlValidationError]): Unit = {
    scenario.write(s"""<h3>HTML: Found ${results.size} issues on "${driver.getTitle}" (${driver.getCurrentUrl})</h3>\n${HtmlValidationReporter.makeTable(results)}""")
  }

  override def writeScenarioResults(scenario: Scenario, results: Seq[HtmlValidationError]): Unit = {
    scenario.write(HtmlValidationReporter.makeSummary(scenarioResults))
  }

  override def prettyName(): String = "HTML Validation Tester"
}

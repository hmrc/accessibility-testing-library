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

package uk.gov.hmrc.accessibility.audit

import java.util.logging.Logger

import cucumber.api.Scenario
import org.openqa.selenium.{JavascriptExecutor, WebDriver}
import uk.gov.hmrc.accessibility.{AccessibilityChecker, CachingChecker, CucumberAccessibilityTester}

object AuditTester {
  val logger: Logger = Logger.getLogger(getClass.getName)

  def initialise(
    driver: WebDriver,
    testerConstructor: (WebDriver with JavascriptExecutor => AuditTester) = new AuditTester(_)): Option[AuditTester] =
    try {
      driver.asInstanceOf[JavascriptExecutor] // Forces cast to the see if type actually matches
      val executor = driver.asInstanceOf[WebDriver with JavascriptExecutor]
      logger.info("Current driver class can execute JavaScript so accessibility is enabled.")
      Some(testerConstructor(executor))
    } catch {
      case ex: ClassCastException => {
        logger.warning(
          s"""Current driver class '${driver.getClass.getName}' cannot be cast to JavascriptExecutor so accessibility
              | testing will not return results.""".stripMargin)
        None
      }
    }
}

class AuditTester(
  driver: WebDriver with JavascriptExecutor,
  checkerCons: (WebDriver with JavascriptExecutor => AccessibilityChecker[AuditResult]) = driver =>
    new CachingChecker[AuditResult](new CodeSniffer(driver)))
    extends CucumberAccessibilityTester[AuditResult] {
  val checker: AccessibilityChecker[AuditResult] = checkerCons(driver)

  override def executeTest(pageSource: String): Seq[AuditResult] =
    checker.run(pageSource)

  def writeStepResults(scenario: Scenario, results: Seq[AuditResult]): Unit =
    scenario.write(
      s"""<h3>Audit: Found ${results.size} issues on "${driver.getTitle}" (${driver.getCurrentUrl})</h3>\n${AuditReporter
        .makeTable(results)}""")

  override def writeScenarioResults(scenario: Scenario, results: Seq[AuditResult]): Unit =
    scenario.write(AuditReporter.makeSummary(scenarioResults))

  override def prettyName(): String = "Audit Tester"
}

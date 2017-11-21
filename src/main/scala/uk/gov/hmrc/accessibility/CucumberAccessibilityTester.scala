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

import java.util.logging.Logger

import cucumber.api.Scenario

trait CucumberAccessibilityTester[T] {
  val logger: Logger = Logger.getLogger(getClass.getName)
  var currentScenario: Option[Scenario] = None
  var scenarioResults: Seq[T] = Seq.empty
  var totalResults: Int = 0

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run:  Unit = println(s"""${prettyName()}: ${totalResults} results found""")
  })

  def startScenario(scenario : Scenario) : Unit = {
    currentScenario = Some(scenario)
    scenarioResults = Seq.empty
  }

  def checkContent(pageSource : String, filter : T => Boolean = _ => true) : Seq[T] = {
    currentScenario match {
      case Some(s) => {
        val stepResults = executeTest(pageSource).filter(filter)
        if (stepResults.nonEmpty) {
          scenarioResults ++= stepResults
          writeStepResults(s, stepResults)
        }
        stepResults
      }
      case None => {
        logger.severe("checkContent has been called without startScenario so no checking will be done.")
        Seq.empty[T]
      }
    }
  }

  def endScenario() : Unit = {
    currentScenario match {
      case Some(s) => {
        writeScenarioResults(s, scenarioResults)
        totalResults += scenarioResults.size
        scenarioResults = Seq.empty
        currentScenario = None
      }
      case None => {
        logger.severe("endScenario has been called without startScenario so behaviour is undefined.")
      }
    }
  }

  def executeTest(pageSource: String): Seq[T]

  def writeStepResults(scenario: Scenario, results: Seq[T]): Unit

  def writeScenarioResults(scenario: Scenario, results: Seq[T]): Unit

  def prettyName(): String

}

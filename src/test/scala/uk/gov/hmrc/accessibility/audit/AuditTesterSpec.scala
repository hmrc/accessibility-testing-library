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

import java.util

import cucumber.api.Scenario
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.openqa.selenium.WebDriver.{Navigation, Options, TargetLocator}
import org.openqa.selenium.{By, JavascriptExecutor, WebDriver, WebElement}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class AuditTesterSpec extends WordSpec with Matchers with MockitoSugar {

  // Needed because Mockito doesn't seem to get along with subtyping multiple classes
  trait WebDriverWithJSExecutor extends WebDriver with JavascriptExecutor

  val scenario = mock[Scenario]

  class InsufficientDriver extends WebDriver {
    override def getPageSource: String = ???
    override def findElements(by: By): util.List[WebElement] = ???
    override def getWindowHandle: String = ???
    override def get(url: String): Unit = ???
    override def manage(): Options = ???
    override def getWindowHandles: util.Set[String] = ???
    override def switchTo(): TargetLocator = ???
    override def close(): Unit = ???
    override def quit(): Unit = ???
    override def getCurrentUrl: String = ???
    override def navigate(): Navigation = ???
    override def getTitle: String = ???
    override def findElement(by: By): WebElement = ???
  }

  class SufficientDriver extends InsufficientDriver with JavascriptExecutor {
    override def executeAsyncScript(script: String, args: AnyRef*): AnyRef = ???
    override def executeScript(script: String, args: AnyRef*): AnyRef = ???
  }

  class EmptyPageDriver extends SufficientDriver {
    override def executeAsyncScript(script: String, args: AnyRef*): AnyRef = {
      AnyRef
    }
    override def getPageSource: String = {
      ""
    }
    override def getTitle: String = "Empty page"
    override def getCurrentUrl: String = "emptypage.com"
  }

  class NonEmptyPageDriver extends EmptyPageDriver {
    override def getPageSource: String = {
      "A non-empty page"
    }
    override def getTitle: String = "Non-empty page"
    override def getCurrentUrl: String = "nonemptypage.com"
  }

  val insufficientDriver = new InsufficientDriver
  val sufficientDriver = new SufficientDriver
  val emptyPageDriver = new EmptyPageDriver
  val nonEmptyPageDriver = new NonEmptyPageDriver

  "the companion object" can {

    "initialise with a compatible driver" should {
      "return indicate initialise was successful" in {
        AuditTester.initialise(sufficientDriver) shouldBe defined
      }
    }

    "initialise with an incompatible driver" should {
      "return indicate initialise was unsuccessful" in {
        AuditTester.initialise(insufficientDriver) shouldBe None
      }
    }
  }

  "the tester class" can {

    "provide empty feedback for an empty page" should {
      val scenario = mock[Scenario]
      val codeSniffer = mock[CodeSniffer]
      when(codeSniffer.run(any())).thenReturn(Seq())
      val driver = spy(emptyPageDriver)

      val tester = new AuditTester(driver, _ => codeSniffer)

      "start with empty results" in {
        tester.scenarioResults shouldEqual Seq.empty
      }

      "not write results if end scenario is called before start scenario" in {
        tester.endScenario()
        verify(scenario, times(0)).write(any())
      }

      "not perform checking if check content is called before start scenario" in {
        tester.checkContent("")
        verify(codeSniffer, times(0)).run(any())
        verify(scenario, times(0)).write(any())
      }

      "have empty results when starting a new scenario" in {
        tester.startScenario(scenario)
        tester.scenarioResults shouldBe empty
      }

      "have empty results after testing an empty page" in {
        val results = tester.checkContent(driver.getPageSource)
        results shouldBe empty
        tester.scenarioResults shouldBe empty
      }

      "have called the checker and driver once after testing one page" in {
        verify(codeSniffer, times(1)).run(any())
        verify(driver, times(1)).getPageSource
      }

      "have written no results to the scenario after testing an empty page" in {
        verify(scenario, times(0)).write(any())
      }

      "have an empty results when ending a scenario" in {
        tester.endScenario()
        tester.scenarioResults shouldEqual Seq.empty
      }
    }

    "provide feedback for a non-empty page" should {
      val scenario = mock[Scenario]
      val codeSniffer = mock[CodeSniffer]
      val driver = spy(nonEmptyPageDriver)
      val warnings = Seq(
        AuditResult("WARNING", "WCAG 2.0", "<a>", "thing", "A description", "<a id='thing'>...</a>")
      )
      val errors = Seq(
        AuditResult("ERROR", "WCAG 2.0", "<a>", "thing", "A description", "<a id='thing'>...</a>"),
        AuditResult("ERROR", "WCAG 2.0", "<a>", "thing", "A description", "<a id='thing'>...</a>")
      )
      when(codeSniffer.run(any())).thenReturn(warnings ++ errors)
      val tester = new AuditTester(driver, _ => codeSniffer)

      "start with empty results" in {
        tester.scenarioResults shouldEqual Seq.empty
      }

      "not write results if end scenario is called before start scenario" in {
        tester.endScenario()
        verify(scenario, times(0)).write(any())
      }

      "not perform checking if check content is called before start scenario" in {
        tester.checkContent("")
        verify(codeSniffer, times(0)).run(any())
        verify(scenario, times(0)).write(any())
      }

      "have empty results when starting a new scenario" in {
        tester.startScenario(scenario)
        tester.scenarioResults shouldBe empty
      }

      "start with an empty results" in {
        tester.startScenario(scenario)
        tester.scenarioResults shouldEqual Seq.empty
      }

      "have the correct accumulated results after calling checkContent" in {
        tester.checkContent(driver.getPageSource)
        tester.scenarioResults shouldBe warnings ++ errors
      }

      "call the underlying code sniffer and driver once when checking one page" in {
        verify(codeSniffer, times(1)).run(any())
        verify(driver, times(1)).getPageSource
      }

      "write a single summary to the scenario" in {
        verify(scenario, times(1)).write(any())
        verify(scenario, times(1)).write(contains("Found 3 issues on \"Non-empty page\" (nonemptypage.com)"))
      }

      "write a summary for the scenario upon completion" in {
        tester.endScenario()
        verify(scenario, times(1)).write(contains("There were 2 error(s) and 1 warning(s)"))
      }
    }
  }
}

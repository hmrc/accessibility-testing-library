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

import java.util

import cucumber.api.Scenario
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.openqa.selenium.WebDriver.{Navigation, Options, TargetLocator}
import org.openqa.selenium.{By, JavascriptExecutor, WebDriver, WebElement}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.accessibility.AccessibilityChecker
import uk.gov.hmrc.accessibility.validation.ValidationRunner

class HtmlValidationTesterSpec extends WordSpec with Matchers with MockitoSugar {

  class BaseDriver extends WebDriver with JavascriptExecutor {
    override def executeAsyncScript(script: String, args: AnyRef*): AnyRef =
      AnyRef
    override def executeScript(script: String, args: AnyRef*): AnyRef = ???
    override def getPageSource: String                                = ???
    override def findElements(by: By): util.List[WebElement]          = ???
    override def getWindowHandle: String                              = ???
    override def get(url: String): Unit                               = ???
    override def manage(): Options                                    = ???
    override def getWindowHandles: util.Set[String]                   = ???
    override def switchTo(): TargetLocator                            = ???
    override def close(): Unit                                        = ???
    override def quit(): Unit                                         = ???
    override def getCurrentUrl: String                                = ???
    override def navigate(): Navigation                               = ???
    override def getTitle: String                                     = ???
    override def findElement(by: By): WebElement                      = ???
  }

  class EmptyPageDriver extends BaseDriver {
    override def getPageSource: String =
      ""
    override def getTitle: String      = "Empty page"
    override def getCurrentUrl: String = "emptypage.com"
  }

  class NonEmptyPageDriver extends EmptyPageDriver {
    override def getPageSource: String =
      "A non-empty page"
    override def getTitle: String      = "Non-empty page"
    override def getCurrentUrl: String = "nonemptypage.com"
  }

  val emptyPageDriver    = new EmptyPageDriver
  val nonEmptyPageDriver = new NonEmptyPageDriver

  "the companion object" should {

    "initialise with a compatible driver" in {
      HtmlValidationTester.initialise(emptyPageDriver)    shouldBe defined
      HtmlValidationTester.initialise(nonEmptyPageDriver) shouldBe defined
    }

  }

  "the tester class" should {
    val scenario = mock[Scenario]
    val runner   = mock[ValidationRunner]
    val checker  = mock[AccessibilityChecker[HtmlValidationError]]

    val tester = new HtmlValidationTester(emptyPageDriver, runner, (_) => checker)

    "call underlying checker to analyse a page" in {
      tester.executeTest("")
      verify(checker, times(1)).run(any())
    }

    "write step results to scenario" in {
      tester.writeStepResults(scenario, Seq.empty)
      val output =
        """<h3>HTML: Found 0 issues on "Empty page" (emptypage.com)</h3>
          |<table><thead><tr><th>Message</th><th>Extract</th><th>Location</th></tr></thead></tbody></table>""".stripMargin
      verify(scenario).write(output)
    }

    "write scenario results to scenario" in {
      tester.writeScenarioResults(scenario, Seq.empty)
      verify(scenario).write(HtmlValidationReporter.makeSummary(Seq.empty))
    }
  }
}

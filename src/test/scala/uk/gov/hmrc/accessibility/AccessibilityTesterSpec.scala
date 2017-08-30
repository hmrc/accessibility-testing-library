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

import java.util

import cucumber.api.Scenario
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.{By, JavascriptExecutor, WebDriver, WebElement}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.{ArgumentCaptor, Mockito}
import org.openqa.selenium.WebDriver.{Navigation, Options, TargetLocator}
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.accessibility.AccessibilityReport._

class AccessibilityTesterSpec extends WordSpec with Matchers with MockitoSugar {

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
  }

  val insufficientDriver = new InsufficientDriver
  val sufficientDriver = new SufficientDriver
  val emptyPageDriver = new EmptyPageDriver

  "the companion object" can {

    "initialise with a compatible driver" should {
      "return indicate initialise was unsuccessful" in {
        AccessibilityTester.initialise(sufficientDriver) shouldBe true
      }

      "not call the tester instance" in {
        val tester = mock[AccessibilityTester]
        AccessibilityTester.initialise(sufficientDriver, _ => tester)
        AccessibilityTester.startScenario(scenario)
        AccessibilityTester.endScenario()

        verify(tester, times(1)).startScenario(any())
        verify(tester, times(1)).endScenario()
      }
    }

    "initialise with an incompatible driver" should {
      "return indicate initialise was unsuccessful" in {
        AccessibilityTester.initialise(insufficientDriver) shouldBe false
      }

      "not call the tester instance" in {
        val tester = mock[AccessibilityTester]
        AccessibilityTester.initialise(insufficientDriver, _ => tester)
        AccessibilityTester.startScenario(scenario)
        AccessibilityTester.endScenario()

        verify(tester, times(0)).startScenario(any())
        verify(tester, times(0)).endScenario()
      }
    }
  }

  "the tester class" can {

    "provide empty feedback for an empty page" should {
      val scenario = mock[Scenario]
      val codeSniffer = mock[CodeSniffer]
      when(codeSniffer.run()).thenReturn(Seq())

      val tester = new AccessibilityTester(emptyPageDriver, _ => codeSniffer)

      "start with an empty cache" in {
        tester.scenarioResults shouldEqual Seq.empty
      }

      "have an empty results when starting a new scenario" in {
        tester.startScenario(scenario)
        tester.cache shouldEqual Map.empty
      }

      "have a cache with one item with no results" in {
        tester.checkContent()
        tester.cache.size shouldBe 1
        tester.cache should contain("da39a3ee5e6b4b0d3255bfef95601890afd80709" -> Seq.empty[AccessibilityResult])
      }

      "write no results to the scenario" in {
        verify(scenario, times(0)).write(any())
      }

      "have an empty results when ending a scenario" in {
        tester.endScenario()
        tester.scenarioResults shouldEqual Seq.empty
      }

      "maintain cache entries between scenarios" in {
        tester.cache.size shouldBe 1
        tester.cache should contain("da39a3ee5e6b4b0d3255bfef95601890afd80709" -> Seq.empty[AccessibilityResult])
      }
    }
  }
}

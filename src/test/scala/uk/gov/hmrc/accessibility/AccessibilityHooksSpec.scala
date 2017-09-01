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

import java.util.logging.Level

import cucumber.api.Scenario
import org.mockito.Answers
import org.mockito.Matchers._
import org.mockito.Mockito.{withSettings, _}
import org.openqa.selenium.logging.{LogEntries, LogEntry}
import org.openqa.selenium.{JavascriptExecutor, WebDriver}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConverters._

class AccessibilityHooksSpec extends WordSpec with Matchers with MockitoSugar {

  val scenario = mock[Scenario]

  class AccessibilityHooksWrapper(runner : AccessibilityRunner) extends AccessibilityHooks {
    override val tester = runner
  }

  "startScenario" should {
    "start the scenario in the underlying tester" in {
      val tester = mock[AccessibilityRunner]
      val wrap = new AccessibilityHooksWrapper(tester)
      wrap.startScenario(scenario)
      verify(tester, times(1)).startScenario(scenario)
    }
  }

  "endScenario" should {
    "end the scenario in the underlying tester" in {
      val tester = mock[AccessibilityRunner]
      val wrap = new AccessibilityHooksWrapper(tester)
      wrap.endScenario()
      verify(tester, times(1)).endScenario()
    }
  }
}

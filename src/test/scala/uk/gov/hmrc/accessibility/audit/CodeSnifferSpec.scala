/*
 * Copyright 2019 HM Revenue & Customs
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

import java.util.logging.Level

import org.mockito.Answers
import org.mockito.Matchers._
import org.mockito.Mockito.{withSettings, _}
import org.openqa.selenium.logging.{LogEntries, LogEntry}
import org.openqa.selenium.{JavascriptExecutor, WebDriver}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConverters._

class CodeSnifferSpec extends WordSpec with Matchers with MockitoSugar {

  val logEntries = Seq(
    """console-api 999:9999 "An unrelated log message"""",
    """console-api 000:1111 "[HTMLCS] NOTICE|WCAG2AA.PrincipleX.GuidelineY_Z.0_0_0.H1,H2|a||Helpful desc|\u003Ca href=\"ref\">...</a>"""",
    """console-api 222:3333 "[HTMLCS] NOTICE|WCAG2AA.PrincipleX.GuidelineY_Z.0_0_0.H1,H2|a|id|Helpful desc|\u003Ca href=\"ref\">...</a>"""",
    """console-api 444:5555 "[HTMLCS] WARNING|WCAG2AA.PrincipleX.GuidelineY_Z.0_0_0.H1,H2|a||Helpful desc|\u003Ca href=\"ref\">...</a>"""",
    """console-api 666:7777 "[HTMLCS] WARNING|WCAG2AA.PrincipleX.GuidelineY_Z.0_0_0.H1,H2|a|id|Helpful desc|\u003Ca href=\"ref\">...</a>"""",
    """console-api 888:9999 "[HTMLCS] ERROR|WCAG2AA.PrincipleX.GuidelineY_Z.0_0_0.H1,H2|a||Helpful desc|\u003Ca href=\"ref\">...</a>"""",
    """console-api 000:1111 "[HTMLCS] ERROR|WCAG2AA.PrincipleX.GuidelineY_Z.0_0_0.H1,H2|a|id|Helpful desc|\u003Ca href=\"ref\">...</a>""""
  ).map(x => new LogEntry(Level.INFO, 0, x))

  val results = Seq(
    AuditResult(
      "ERROR",
      "WCAG2AA.PrincipleX.GuidelineY_Z.0_0_0.H1,H2",
      "a",
      "",
      "Helpful desc",
      "<a href=\"ref\">...</a>"),
    AuditResult(
      "ERROR",
      "WCAG2AA.PrincipleX.GuidelineY_Z.0_0_0.H1,H2",
      "a",
      "id",
      "Helpful desc",
      "<a href=\"ref\">...</a>"),
    AuditResult(
      "WARNING",
      "WCAG2AA.PrincipleX.GuidelineY_Z.0_0_0.H1,H2",
      "a",
      "",
      "Helpful desc",
      "<a href=\"ref\">...</a>"),
    AuditResult(
      "WARNING",
      "WCAG2AA.PrincipleX.GuidelineY_Z.0_0_0.H1,H2",
      "a",
      "id",
      "Helpful desc",
      "<a href=\"ref\">...</a>")
  )

  // Needed because Mockito doesn't seem to get along with subtyping multiple classes
  trait WebDriverWithJSExecutor extends WebDriver with JavascriptExecutor

  val driver = mock[WebDriverWithJSExecutor](withSettings.defaultAnswer(Answers.RETURNS_DEEP_STUBS.get()))
  when(driver.manage().logs().get(any())).thenReturn(new LogEntries(logEntries.asJava))
  val cs = new CodeSniffer(driver)

  "processLogs" should {
    "ignore log entries at NOTICE level and retain all others" in {
      val grouped = cs.processLogs(logEntries).groupBy(_.level)
      grouped.getOrElse("NOTICE", Seq()).size  shouldBe 0
      grouped.getOrElse("ERROR", Seq()).size   shouldBe 2
      grouped.getOrElse("WARNING", Seq()).size shouldBe 2
    }

    "parse the logs into the correct format and undo escaping of characters" in {
      cs.processLogs(logEntries) shouldEqual results
    }
  }

  "run" should {
    "produce the expected AccessibilityResult instances" in {
      cs.run("") shouldEqual results
    }
  }
}

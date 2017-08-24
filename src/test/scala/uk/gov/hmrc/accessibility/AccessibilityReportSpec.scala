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

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.accessibility.AccessibilityReport._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class AccessibilityReportSpec extends WordSpec with Matchers with MockitoSugar {

  private val errorResult = AccessibilityResult("ERROR","standard","element","identifier","description", "context")
  private val warningResult = AccessibilityResult("WARNING","standard","element","identifier","description", "context")

  private def makeSeq(errors : Int, warnings : Int) : Seq[AccessibilityResult] = {
    Seq.fill(errors)(errorResult) ++ Seq.fill(warnings)(warningResult)
  }

  val summaries = Table(
    ("errors", "warnings", "message"),
    (0, 0, "There were no errors or warnings"),
    (1, 0, "There was 1 error and no warnings"),
    (0, 1, "There were no errors and 1 warning"),
    (2, 0, "There were 2 errors and no warnings"),
    (0, 2, "There were no errors and 2 warnings"),
    (2, 2, "There were 2 errors and 2 warnings")
  )
  
  "makeScenarioSummary" should {
    forAll(summaries) { (e, w, msg) =>
      s"give the message '$msg' when there are $e errors and $w warnings" in {
        makeScenarioSummary(makeSeq(e,w)) should include(msg)
      }
    }
  }

  private def checkTableHead(doc : Document): Unit = {
    doc.getElementsByTag("table").size() shouldBe 1
    doc.getElementsByTag("thead").size() shouldBe 1
    doc.select("thead tr").size() shouldBe 1
  }

  private def checkTableRowCount(doc : Document, errors : Int, warnings : Int): Unit = {
    doc.getElementsContainingOwnText("ERROR").size() shouldBe errors
    doc.getElementsContainingOwnText("WARNING").size() shouldBe warnings
  }

  "makeTable" should {
    "contain a header and no rows for empty result sequence" in {
      val output: Document = Jsoup.parse(makeTable(Seq()))
      checkTableHead(output)
      checkTableRowCount(output, 0, 0)
    }

    "contain a header and one row for result sequence with 1 error" in {
      println(makeSeq(1, 1))
      val output: Document = Jsoup.parse(makeTable(makeSeq(1, 0)))
      checkTableHead(output)
      checkTableRowCount(output, 1, 0)
    }

    "contain a header and one row for result sequence with 1 warning" in {
      val output: Document = Jsoup.parse(makeTable(makeSeq(0, 1)))
      checkTableHead(output)
      checkTableRowCount(output, 0, 1)
    }

    "contain a header and two rows for result sequence with 1 error and 1 warning" in {
      val output: Document = Jsoup.parse(makeTable(makeSeq(1, 1)))
      checkTableHead(output)
      checkTableRowCount(output, 1, 1)
    }

    "contain a header and four rows for result sequence with 2 errors and 2 warnings" in {
      val output: Document = Jsoup.parse(makeTable(makeSeq(2, 2)))
      checkTableHead(output)
      checkTableRowCount(output, 2, 2)
    }
  }
}

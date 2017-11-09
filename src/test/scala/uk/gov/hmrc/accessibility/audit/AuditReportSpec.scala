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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.accessibility.audit.AuditReport._

class AuditReportSpec extends WordSpec with Matchers with MockitoSugar {

  private val errorResult = AuditResult("ERROR","standard","element","identifier","description", "context")
  private val warningResult = AuditResult("WARNING","standard","element","identifier","description", "context")

  private def makeSeq(errors : Int, warnings : Int) : Seq[AuditResult] = {
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

  val tables = Table(
    ("errors", "warnings", "message"),
    (0, 0, "contain a header and no rows for empty result sequence"),
    (1, 0, "contain a header and one row for result sequence with 1 error"),
    (0, 1, "contain a header and one row for result sequence with 1 warning"),
    (1, 1, "contain a header and two rows for result sequence with 1 error and 1 warning"),
    (2, 2, "contain a header and four rows for result sequence with 2 errors and 2 warnings")
  )

  "makeTable" should {
    forAll(tables) {(e, w, msg) =>
      msg in {
        val output: Document = Jsoup.parse(makeTable(makeSeq(e, w)))
        checkTableHead(output)
        checkTableRowCount(output, e, w)
      }
    }
  }
}

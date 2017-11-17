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

package uk.gov.hmrc.accessibility.validation.html

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.accessibility.validation.html.HtmlValidationReporter._

class HtmlReporterSpec extends WordSpec with Matchers with MockitoSugar {

  private val validationError = HtmlValidationError(1, 2, 3, "message", "<extract>")

  val reporter = HtmlValidationReporter

  val summaries = Table(
    ("errors", "message"),
    (0, "There were 0 issue(s)."),
    (1, "There were 1 issue(s)."),
    (2, "There were 2 issue(s).")
  )

  "summaryContent" should {
    forAll(summaries) { (errors, message) =>
      s"give the message '$message' when there are $errors errors" in {
        reporter.summaryContent(Seq.fill(errors)(validationError)) shouldBe message
      }
    }
  }

  private def checkTableHead(doc : Document): Unit = {
    doc.getElementsByTag("table").size() shouldBe 1
    doc.getElementsByTag("thead").size() shouldBe 1
    doc.select("thead tr").size() shouldBe 1
  }

  private def checkTableRowCount(doc : Document, errors : Int): Unit = {
    doc.getElementsContainingOwnText("extract>").size() shouldBe errors
  }

  val tables = Table(
    ("errors", "message"),
    (0, "container a header and 0 rows for empty result sequence"),
    (1, "container a header and 1 rows for result sequence with 1 error"),
    (2, "container a header and 2 rows for result sequence with 2 error"),
    (3, "container a header and 3 rows for result sequence with 3 error")
  )

  "makeTable" should {
    forAll(tables) {(errors, message) =>
      message in {
        val result = makeTable(Seq.fill(errors)(validationError))
        val output: Document = Jsoup.parse(result)
        checkTableHead(output)
        checkTableRowCount(output, errors)
      }
    }
  }
}

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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.accessibility.audit.AuditReporter._

class AuditReporterSpec extends WordSpec with Matchers with MockitoSugar {

  private val errorResult = AuditResult("ERROR","standard","element","identifier","description", "context")
  private val warningResult = AuditResult("WARNING","standard","element","identifier","description", "context")

  private def makeSeq(errors : Int, warnings : Int) : Seq[AuditResult] = {
    Seq.fill(errors)(errorResult) ++ Seq.fill(warnings)(warningResult)
  }

  val summaries = Table(
    ("errors", "warnings", "message"),
    (0, 0, "There were 0 error(s) and 0 warning(s)"),
    (1, 0, "There were 1 error(s) and 0 warning(s)"),
    (0, 1, "There were 0 error(s) and 1 warning(s)"),
    (2, 0, "There were 2 error(s) and 0 warning(s)"),
    (0, 2, "There were 0 error(s) and 2 warning(s)"),
    (2, 2, "There were 2 error(s) and 2 warning(s)")
  )
  
  "makeScenarioSummary" should {
    forAll(summaries) { (e, w, msg) =>
      s"give the message '$msg' when there are $e errors and $w warnings" in {
        summaryContent(makeSeq(e,w)) should include(msg)
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

  def makeStandardLink(standard: String): String = s"https://squizlabs.github.io/HTML_CodeSniffer/Standards/WCAG2/$standard#sniff-coverage"
  def makeTechniqueLink(technique: String): String = s"https://www.w3.org/TR/WCAG20-TECHS/$technique"

  val links = Table(
    ("input", "standard", "technique"),
    ("WCAG2AA.Principle1.Guideline1_4.1_4_3.G18.Abs", "1_4_3", "G18"),
    ("WCAG2AA.Principle4.Guideline4_1.4_1_1.F77", "4_1_1", "F77"),
    ("WCAG2AA.Principle1.Guideline1_3.1_3_1_A.G141", "1_3_1", "G141"),
    ("WCAG2AA.Principle4.Guideline4_1.4_1_2.H91.Fieldset.Name", "4_1_2", "H91"),
    ("WCAG2AA.Principle1.Guideline1_3.1_3_1.H71.NoLegend", "1_3_1", "H71")
  )

  "linkStandard" should {
    forAll(links) {(input, standard, technique) =>
      s"give correct links for $input" in {
        linkStandard(input) shouldBe s""" <a href="${makeStandardLink(standard)}">(Description)</a> <a href="${makeTechniqueLink(technique)}">(Standard)</a> """
      }
    }
  }
}

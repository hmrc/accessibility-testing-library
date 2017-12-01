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

import uk.gov.hmrc.accessibility.AccessibilityReporter

object AuditReporter extends AccessibilityReporter[AuditResult] {

  val regex = """WCAG2[A]+\.Principle\d\.Guideline\w+\.([\d_]+\d)[\w]*\.(\w+).*""".r

  override val ReportName: String = "Audit summary"

  override def headingValues(): Seq[String] = Seq(
    "Level",
    "Standard",
    "Element",
    "Detail",
    "Description",
    "Context"
  )

  override def rowValues(item: AuditResult): Seq[String] = {
    Seq(
      item.level,
      item.standard + linkStandard(item.standard),
      item.element,
      item.identifier,
      item.description,
      item.context
    )
  }

  override def summaryContent(data: Seq[AuditResult]): String = {
    val grouped = data.groupBy(_.level)
    s"There were ${grouped.getOrElse("ERROR", Seq.empty).size} error(s) " +
      s"and ${grouped.getOrElse("WARNING", Seq.empty).size} warning(s)."
  }

  def linkStandard(standard: String): String = {
    standard match {
      case regex(reference, technique) => {
        val referenceUrl = s"https://squizlabs.github.io/HTML_CodeSniffer/Standards/WCAG2/$reference#sniff-coverage"
        val techniqueUrl = s"https://www.w3.org/TR/WCAG20-TECHS/$technique"
        s""" <a href="$referenceUrl">(Description)</a> <a href="$techniqueUrl">(Standard)</a> """
      }
      case _ => ""
    }
  }

}

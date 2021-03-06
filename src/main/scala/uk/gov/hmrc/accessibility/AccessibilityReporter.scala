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

package uk.gov.hmrc.accessibility

trait AccessibilityReporter[T] {
  val TableHeaderStart   = """<table><thead><tr>"""
  val TableHeaderEnd     = """</tr></thead>"""
  val TableFooter        = """</tbody></table>"""
  val ReportName: String = "Accessibility summary"
  lazy val SummaryHeader = s"""<h3>$ReportName</h3><p>"""
  val SummaryFooter      = "</p>"

  def makeTable(data: Seq[T]): String = {
    var output = new StringBuilder(TableHeaderStart)
    output ++= headingValues().map(v => s"<th>$v</th>").mkString
    output ++= TableHeaderEnd
    data.map(r => {
      output ++= "<tr>"
      rowValues(r).foreach(v => output ++= s"<td>$v</td>")
      output ++= "</tr>"
    })
    output ++= TableFooter
    output.toString()
  }

  def makeSummary(data: Seq[T]): String = {
    var output = new StringBuilder(SummaryHeader)
    output ++= summaryContent(data)
    output ++= SummaryFooter
    output.toString()
  }

  def headingValues(): Seq[String]
  def rowValues(item: T): Seq[String]
  def summaryContent(data: Seq[T]): String
}

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

object AccessibilityReport {
  def makeTable(data : Seq[AccessibilityResult]) : String = {
    var output = new StringBuilder("""
                                     | <table style="margin-top: -4em">
                                     |   <thead>
                                     |     <tr>
                                     |       <th>Level</th>
                                     |       <th>Standard</th>
                                     |       <th>Element</th>
                                     |       <th>Detail</th>
                                     |       <th>Description</th>
                                     |       <th>Context</th>
                                     |     </tr>
                                     |   </thead>
                                     |   <tbody>
                                     |""".stripMargin)
    data.foreach(row => {
      output ++= s"""
                    |<tr>
                    | <td>${row.level}</td>
                    | <td>${row.standard}</td>
                    | <td>${row.element}</td>
                    | <td>${row.identifier}</td>
                    | <td>${row.description}</td>
                    | <td>${row.context}</td>
                    |</tr>
         """.stripMargin
    })
    output ++= "</tbody></table>"
    output.toString()
  }

  def makeScenarioSummary(scenarioResults : Seq[AccessibilityResult]) : String = {
    var output = new StringBuilder("""<h3>Summary</h3><p style="margin-top: -1em">There """)
    val grouped = scenarioResults.groupBy(_.level)
    (grouped.getOrElse("ERROR", Seq.empty).size, grouped.getOrElse("WARNING", Seq.empty).size) match {
      case (0, 0) => output ++= "were no errors or warnings"
      case (1, 0) => output ++= "was 1 error and no warnings"
      case (0, 1) => output ++= "were no errors and 1 warning"
      case (e, 0) => output ++= s"were $e errors and no warnings"
      case (0, w) => output ++= s"were no errors and $w warnings"
      case (e, w) => output ++= s"were $e errors and $w warnings"
    }
    output ++= "</p>"
    output.toString()
  }
}

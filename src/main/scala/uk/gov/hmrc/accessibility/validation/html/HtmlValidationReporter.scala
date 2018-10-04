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

package uk.gov.hmrc.accessibility.validation.html

import uk.gov.hmrc.accessibility.AccessibilityReporter

object HtmlValidationReporter extends AccessibilityReporter[HtmlValidationError] {

  override val ReportName: String = "HTML validation summary"

  override def headingValues(): Seq[String] = Seq(
    "Message",
    "Extract",
    "Location"
  )

  override def rowValues(item: HtmlValidationError): Seq[String] =
    Seq(
      item.message,
      item.extract.replace("\u003C", "&#x003C;"),
      s"${item.line}:${item.startCol}-${item.endCol}"
    )

  override def summaryContent(data: Seq[HtmlValidationError]): String =
    s"There were ${data.size} issue(s)."
}

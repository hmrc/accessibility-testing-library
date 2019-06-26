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

package uk.gov.hmrc.accessibility.validation.html

import uk.gov.hmrc.accessibility.ResultFilters

object HtmlValidationFilters extends ResultFilters[HtmlValidationError] {

  val headerIcon = """A “link” element with a “sizes” attribute must
               | have a “rel” attribute that contains the value
               | “icon” or the value “apple-touch-icon”.""".stripMargin.replace("\n", "")

  def headerFooterFilter: PartialFunction[HtmlValidationError, Boolean] = {
    case HtmlValidationError(_, _, _, `headerIcon`, _)                                                      => false
    case HtmlValidationError(_, _, _, """The “banner” role is unnecessary for element “header”.""", _)      => false
    case HtmlValidationError(_, _, _, """The “navigation” role is unnecessary for element “nav”.""", _)     => false
    case HtmlValidationError(_, _, _, """The “main” role is unnecessary for element “main”.""", _)          => false
    case HtmlValidationError(_, _, _, """The “contentinfo” role is unnecessary for element “footer”.""", _) => false
  }

  def knownErrorsFilter: PartialFunction[HtmlValidationError, Boolean] = {
    // We include the "list" role on ul and ol because of a VoiceOver bug
    case HtmlValidationError(_, _, _, """The “list” role is unnecessary for element “ul”.""", _) => false
    case HtmlValidationError(_, _, _, """The “list” role is unnecessary for element “ol”.""", _) => false

    // The polyfill for old browsers causes the below validation error
    case HtmlValidationError(_, _, _, """The “button” role is unnecessary for element “summary”.""", _) => false
  }

}

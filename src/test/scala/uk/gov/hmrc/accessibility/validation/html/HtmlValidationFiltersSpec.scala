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

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.accessibility.validation.html.HtmlValidationFilters._

class HtmlValidationFiltersSpec extends WordSpec with Matchers with MockitoSugar {

  private lazy val combinedResults: Seq[HtmlValidationError] = dummyErrors ++ headerFooterErrors ++ knownErrors

  private val headerFooterErrors: Seq[HtmlValidationError] = Seq(
    HtmlValidationError(1, 2, 3, HtmlValidationFilters.headerIcon, ""),
    HtmlValidationError(1, 2, 3, """The “banner” role is unnecessary for element “header”.""", ""),
    HtmlValidationError(1, 2, 3, """The “navigation” role is unnecessary for element “nav”.""", ""),
    HtmlValidationError(1, 2, 3, """The “main” role is unnecessary for element “main”.""", ""),
    HtmlValidationError(1, 2, 3, """The “contentinfo” role is unnecessary for element “footer”.""", "")
  )

  private val knownErrors: Seq[HtmlValidationError] = Seq(
    HtmlValidationError(1, 2, 3, """The “list” role is unnecessary for element “ul”.""", ""),
    HtmlValidationError(1, 2, 3, """The “list” role is unnecessary for element “ol”.""", ""),
    HtmlValidationError(1, 2, 3, """The “button” role is unnecessary for element “summary”.""", "")
  )

  private val dummyErrors: Seq[HtmlValidationError] = Seq(
    HtmlValidationError(1, 2, 3, "messageA", "extractA"),
    HtmlValidationError(1, 2, 3, "messageB", "extractB")
  )

  "emptyFilter" should {
    "retain all results" in {
      combinedResults.count(emptyFilter) shouldBe combinedResults.size
    }
  }

  "headerFooterFilter" should {
    "only remove results about the header and footer" in {
      val filtered = combinedResults.filter(headerFooterFilter orElse emptyFilter)
      filtered.size should be(combinedResults.size - headerFooterErrors.size)
      filtered      should not contain headerFooterErrors
    }
  }

  "knownErrorsFilter" should {
    "only remove results about known errors" in {
      val filtered = combinedResults.filter(knownErrorsFilter orElse emptyFilter)
      filtered.size should be(combinedResults.size - knownErrors.size)
      filtered      should not contain knownErrors
    }
  }
}

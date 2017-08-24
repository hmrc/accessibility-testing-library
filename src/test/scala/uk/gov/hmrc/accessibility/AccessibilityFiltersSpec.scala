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
import uk.gov.hmrc.accessibility.AccessibilityFilters._

class AccessibilityFiltersSpec extends WordSpec with Matchers with MockitoSugar {

  private lazy val combinedResults = dummyResults ++ webChatResults ++ headerFooterResults

  private val headerFooterResults : Seq[AccessibilityResult] = Seq(
    AccessibilityResult("ERROR","WCAG2AA.Principle1.Guideline1_4.1_4_3.G18.Fail","strong","","","&#x003C;strong class=\"phase-tag\">...&#x003C;/strong>"),
    AccessibilityResult("ERROR","WCAG2AA.Principle4.Guideline4_1.4_1_2.H91.InputEmail.Name","input","#report-email","","&#x003C;input id=\"report-email\" maxlength=\"255\" class=\"input--fullwidth form-control\" name=\"report-email\" type=\"email\" data-rule-required=\"true\" data-rule-email=\"true\" data-msg-required=\"Please provide your email address.\" aria-required=\"true\">"),
    AccessibilityResult("WARNING","WCAG2AA.Principle1.Guideline1_1.1_1_1.H67.2","img","","","template/assets/images/gov.uk_logotype_crown.png\" alt=\"\">"),
    AccessibilityResult("WARNING","WCAG2AA.Principle1.Guideline1_4.1_4_3.G18.BgImage","span","","","&#x003C;span class=\"organisation-logo organisation-logo-medium\">...&#x003C;/span>"),
    AccessibilityResult("WARNING","WCAG2AA.Principle1.Guideline1_4.1_4_3.G18.BgImage","a","","","&#x003C;a href=\"http://www.nationalarchives.gov.uk/information-management/our-services/crown-copyright.htm\" target=\"_blank\">...&#x003C;/a>")
  )

  private val dummyResults : Seq[AccessibilityResult] = Seq(
    AccessibilityResult("ERROR","","","","",""),
    AccessibilityResult("WARNING","","","","","")
  )

  private val webChatResults : Seq[AccessibilityResult] = Seq(
    AccessibilityResult("ERROR","WCAG2AA.Principle2.Guideline2_4.2_4_1.H64.1","iframe","#egot_iframe","","")
  )

  "emptyFilter" should {
    "retain all results" in {
      combinedResults.count(emptyFilter) shouldBe combinedResults.size
    }
  }

  "webChatFilter" should {
    "only remove results about the web chat iframe" in {
      val filtered = combinedResults.filter(webChatFilter orElse emptyFilter)
      filtered.size should be (combinedResults.size - webChatResults.size)
      filtered should not contain webChatResults
    }
  }

  "headerFooterFilter" should {
    "only remove results about the header and footer" in {
      val filtered = combinedResults.filter(headerFooterFilter orElse emptyFilter)
      filtered.size should be (combinedResults.size - headerFooterResults.size)
      filtered should not contain headerFooterResults
    }
  }
}

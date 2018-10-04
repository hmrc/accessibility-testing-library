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

package uk.gov.hmrc.accessibility

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.mock.MockitoSugar

class CachingCheckerSpec extends WordSpec with Matchers with MockitoSugar {
  def getWrapped(): AccessibilityChecker[String] = {
    val wrappedChecker: AccessibilityChecker[String] = mock[AccessibilityChecker[String]]
    when(wrappedChecker.run(any())).thenReturn(Seq("a", "b"))
    wrappedChecker
  }
  val resultSeq = Seq("a", "b")

  "Caching checker" should {
    val wrapped = getWrapped()
    val caching = new CachingChecker[String](wrapped)

    "start with empty cache" in {
      caching.cache shouldBe empty
    }

    "obtain results from wrapped checker and add to cache" in {
      val results: Seq[String] = caching.run("page")
      results       shouldBe resultSeq
      caching.cache shouldBe Map("767013ce0ee0f6d7a07587912eba3104cfaabc15" -> resultSeq)
      verify(wrapped, times(1)).run(any())
    }

    "use cache values" in {
      val results: Seq[String] = caching.run("page")
      results       shouldBe resultSeq
      caching.cache shouldBe Map("767013ce0ee0f6d7a07587912eba3104cfaabc15" -> resultSeq)
      verify(wrapped, times(1)).run(any())
    }

    "add multiple values to cache" in {
      val results: Seq[String] = caching.run("page2")
      results shouldBe resultSeq
      caching.cache shouldBe Map(
        "767013ce0ee0f6d7a07587912eba3104cfaabc15" -> resultSeq,
        "0fbabd0c141db8c6ad4ec4972592e4f59bfa7217" -> resultSeq)
      verify(wrapped, times(2)).run(any())
    }
  }
}

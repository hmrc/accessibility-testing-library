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

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.accessibility.validation.ValidationRunner

class HtmlValidatorSpec extends WordSpec with Matchers with MockitoSugar {

  "convertJson" should {

    "Give an empty sequence for empty input" in {
      val result = new HtmlValidator().convertJson("")
      result shouldBe Seq()
    }

    "Give an empty sequence for invalid input" in {
      val result = new HtmlValidator().convertJson("Not JSON")
      result shouldBe Seq()
    }

    "Give a sequence of one value for one result" in {
      val json   = """{
                   |  "messages": [
                   |    {
                   |      "type": "error",
                   |      "lastLine": 1,
                   |      "lastColumn": 2,
                   |      "firstColumn": 3,
                   |      "message": "message1",
                   |      "extract": "extract1",
                   |      "hiliteStart": 4,
                   |      "hiliteLength": 5
                   |    }
                   |  ]
                   |}""".stripMargin
      val result = new HtmlValidator().convertJson(json)
      result shouldBe Seq(HtmlValidationError(1, 3, 2, "message1", "extract1"))
    }

    "Give a sequence of multiple values for multiple results" in {
      val json   = """{
                   |  "messages": [
                   |    {
                   |      "type": "error",
                   |      "lastLine": 1,
                   |      "lastColumn": 2,
                   |      "firstColumn": 3,
                   |      "message": "message1",
                   |      "extract": "extract1",
                   |      "hiliteStart": 4,
                   |      "hiliteLength": 5
                   |    },
                   |    {
                   |      "type": "error",
                   |      "lastLine": 6,
                   |      "lastColumn": 7,
                   |      "firstColumn": 8,
                   |      "message": "message2",
                   |      "extract": "extract2",
                   |      "hiliteStart": 9,
                   |      "hiliteLength": 10
                   |    },
                   |    {
                   |      "type": "error",
                   |      "lastLine": 11,
                   |      "lastColumn": 12,
                   |      "firstColumn": 13,
                   |      "message": "message3",
                   |      "extract": "extract3",
                   |      "hiliteStart": 14,
                   |      "hiliteLength": 15
                   |    },
                   |    {
                   |      "type": "error",
                   |      "lastLine": 16,
                   |      "lastColumn": 17,
                   |      "firstColumn": 18,
                   |      "message": "message4",
                   |      "extract": "extract4",
                   |      "hiliteStart": 19,
                   |      "hiliteLength": 20
                   |    }
                   |  ]
                   |}""".stripMargin
      val result = new HtmlValidator().convertJson(json)
      result shouldBe Seq(
        HtmlValidationError(1, 3, 2, "message1", "extract1"),
        HtmlValidationError(6, 8, 7, "message2", "extract2"),
        HtmlValidationError(11, 13, 12, "message3", "extract3"),
        HtmlValidationError(16, 18, 17, "message4", "extract4")
      )
    }
  }

  "validate" should {

    val json = """{
                 |  "messages": [
                 |    {
                 |      "type": "error",
                 |      "lastLine": 1,
                 |      "lastColumn": 2,
                 |      "firstColumn": 3,
                 |      "message": "message1",
                 |      "extract": "extract1",
                 |      "hiliteStart": 4,
                 |      "hiliteLength": 5
                 |    }
                 |  ]
                 |}""".stripMargin

    "Call the runner and produce errors when present" in {
      val runner = mock[ValidationRunner]
      when(runner.run(any())).thenReturn(json)
      val validator = new HtmlValidator(runner)
      val result    = validator.run("")
      result shouldBe Seq(HtmlValidationError(1, 3, 2, "message1", "extract1"))
    }

    "Call the runner and produce no errors when none present" in {
      val runner = mock[ValidationRunner]
      when(runner.run(any())).thenReturn("")
      val validator = new HtmlValidator(runner)
      val result    = validator.run("")
      result shouldBe Seq()
    }

    "Invoke the external dependency and return results from it" in {
      val validator = new HtmlValidator()
      val result    = validator.run("<html></html>")
      result shouldBe Seq(
        HtmlValidationError(
          1,
          1,
          6,
          "Start tag seen without seeing a doctype first. Expected “<!DOCTYPE html>”.",
          "<html></html"),
        HtmlValidationError(
          1,
          7,
          13,
          "Element “head” is missing a required instance of child element “title”.",
          "<html></html>")
      )
    }

  }

}

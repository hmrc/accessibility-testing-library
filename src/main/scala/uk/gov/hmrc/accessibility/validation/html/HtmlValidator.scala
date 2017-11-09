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

package uk.gov.hmrc.accessibility.validation.html

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import play.api.libs.json._

class HtmlValidator(val runner: ValidationRunner = new ProcessValidationRunner) {
  private val Logger = java.util.logging.Logger.getLogger(getClass.getName)

  def validate(source: String): Seq[HtmlError] = {
    val result = runner.run(source)
    convertJson(result)
  }

  def convertJson(result: String): Seq[HtmlError] = {
    if (result == "") {
      Seq()
    } else {
      try {
        val wrapped = Json.parse(result)
        (wrapped \ "messages").as[Seq[HtmlError]]
      } catch {
        case e @ (_ : JsonMappingException| _ : JsonParseException) => {
          Logger.warning(e.getMessage)
          Seq()
        }
      }
    }
  }
}
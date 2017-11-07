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

import java.io.ByteArrayInputStream

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scala.sys.process._

class HtmlValidator {
  private val Logger = java.util.logging.Logger.getLogger(getClass.getName)
  private val Jar = getClass.getClassLoader.getResource("vnu.jar").getPath
  private val Command = Seq("java", "-jar", Jar, "--exit-zero-always", "--format", "json", "-")

  def run(source: String): Seq[HtmlError] = {
    val reader = new ByteArrayInputStream(source.getBytes("UTF-8"))
    val result: String = (Command #< reader).!!
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

case class HtmlError(line: Int, startCol: Int, endCol: Int, message: String, extract: String)

object HtmlError {
  implicit val htmlErrorReads: Reads[HtmlError] = (
    (JsPath \ "lastLine").read[Int] and
      (JsPath \ "firstColumn").read[Int] and
      (JsPath \ "lastColumn").read[Int] and
      (JsPath \ "message").read[String] and
      (JsPath \ "extract").read[String]
    ) (HtmlError.apply _)
}

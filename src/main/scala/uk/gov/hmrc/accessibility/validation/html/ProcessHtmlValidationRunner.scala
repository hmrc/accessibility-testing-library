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

import java.io.{ByteArrayInputStream, File}

import org.apache.commons.io.FileUtils
import uk.gov.hmrc.accessibility.validation.ValidationRunner

import scala.sys.process._

class ProcessHtmlValidationRunner extends ValidationRunner {
  private val Jar = getClass.getClassLoader.getResourceAsStream("vnu.jar")
  FileUtils.copyInputStreamToFile(Jar, new File("vnu.jar"))

  private val Commands = Seq("java", "-jar", "vnu.jar", "--exit-zero-always", "--format", "json", "-")

  override def run(source: String): String = {
    val reader = new ByteArrayInputStream(source.getBytes("UTF-8"))
    var lines = Seq[String]()
    (Commands #< reader).!!(ProcessLogger(line => {lines = lines :+ line}))
    lines.mkString("\n")
  }
}

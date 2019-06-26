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

package uk.gov.hmrc.accessibility.audit

import java.util.logging.Level

import org.openqa.selenium.logging.LogEntry
import org.openqa.selenium.{JavascriptExecutor, WebDriver}
import uk.gov.hmrc.accessibility.AccessibilityChecker

import scala.collection.JavaConverters._
import scala.io.Source

class CodeSniffer(driver: WebDriver with JavascriptExecutor) extends AccessibilityChecker[AuditResult] {
  private val FilePath   = "/HtmlCodeSniffer.js"
  private val JavaScript = Source.fromInputStream(getClass.getResourceAsStream(FilePath)).getLines().mkString("\n")
  private val CSCommand  = "window.HTMLCS_RUNNER.run('WCAG2AA');"
  private val LogRegex   = """^.*?"\[HTMLCS\]\s*(.*)\|(.*)\|(.*)\|(.*)\|(.*)\|(.*)"""".r

  override def run(pageSource: String): Seq[AuditResult] = {
    driver.manage().logs().get("browser") // Clear the unrelated logs
    driver.executeScript(JavaScript)
    driver.executeScript(CSCommand)
    val logs = driver.manage().logs().get("browser").filter(Level.ALL).asScala
    processLogs(logs)
  }

  def processLogs(logs: Seq[LogEntry]): Seq[AuditResult] =
    logs
      .map(_.getMessage)
      .map(_.replace("\\u003C", "&#x003C;").replace("\\\"", "\"")) // Undoing escaping in HtmlCodeSniffer
      .flatMap {
        case LogRegex(level, standard, element, identifier, description, context) =>
          Some(AuditResult(level, standard, element, identifier, description, context))
        case _ => None
      }
      .filter(_.level != "NOTICE")
      .sortBy(_.level)
}

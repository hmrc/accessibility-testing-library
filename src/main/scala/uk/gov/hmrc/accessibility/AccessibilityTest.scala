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

import java.security.MessageDigest
import java.util.logging.{Level, Logger}
import cucumber.api.Scenario
import cucumber.api.java.{After, Before}
import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.{JavascriptExecutor, WebDriver}
import scala.collection.JavaConverters._
import scala.io.Source

class AccessibilityHooks extends ScalaDsl with EN {
  @Before
  def staticScenarioRef(scenario : Scenario): Unit = {
    AccessibilityTester.startScenario(scenario)
  }

  @After
  def outputScenarioSummary(): Unit = {
    AccessibilityTester.endScenario()
  }
}

object AccessibilityTester {
  private var scenario: Scenario = _
  private var scenarioResults : Seq[AccessibilityResult] = Seq.empty

  private var hasRun = false
  private var cache : Map[String,Seq[AccessibilityResult]] = Map.empty
  private lazy val digest = MessageDigest.getInstance("SHA1")
  private lazy val hashcodeOf = {x : String => digest.digest(x.getBytes()).map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}}

  def startScenario(scenario: Scenario): Unit = {
    this.scenario = scenario
    scenarioResults = Seq.empty
  }

  def endScenario(): Unit = {
    scenario.write(AccessibilityResult.makeScenarioSummary(scenarioResults))
    hasRun = false
  }

  def checkContent(driver : WebDriver, filter : AccessibilityResult => Boolean = AccessibilityFilters.emptyFilter) : Unit = {
    try {
      val executor = driver.asInstanceOf[WebDriver with JavascriptExecutor]
      hasRun = true
      val hash = hashcodeOf(executor.getPageSource)

      val data = cache.getOrElse(hash, {
        val results = runCodeSniffer(executor).filter(filter)
        cache = cache + (hash -> results)
        results
      })

      if (data.nonEmpty) {
        scenarioResults ++= data
        scenario.write(s"<h3>Found ${data.size} issues on ${driver.getTitle} (${driver.getCurrentUrl})</h3>\n${AccessibilityResult.makeTable(data)}")
      }

    } catch {
      case ex : ClassCastException => Logger.getLogger(AccessibilityTester.getClass.getName).warning(
        s"""
           |Current driver class '${driver.getClass.getName}' cannot be cast to JavascriptExecutor so accessibility
           | testing will not return results.
           |""".stripMargin)
    }
  }

  private def runCodeSniffer(driver : WebDriver with JavascriptExecutor) : Seq[AccessibilityResult] = {
    // Clear the unrelated logs
    driver.manage().logs().get("browser")
    driver.executeScript(Source.fromInputStream(getClass.getResourceAsStream("/HtmlCodeSniffer.js")).getLines().mkString("\n"))
    driver.executeScript("window.HTMLCS_RUNNER.run('WCAG2AA');")
    val regex = """^.*?"\[HTMLCS\]\s*(.*)\|(.*)\|(.*)\|(.*)\|(.*)\|(.*)"""".r
    val logs = driver.manage().logs().get("browser").filter(Level.ALL)
    logs.asScala
      .map(_.getMessage)
      .map(_.replace("\\u003C","&#x003C;").replace("\\\"","\"")) // Undoing in HtmlCodeSniffer
      .flatMap {
      case regex(level, standard, element, identifier, description, context) =>
        Some(AccessibilityResult(level, standard, element, identifier, description, context))
      case _ => None
    }
      .filter(_.level != "NOTICE")
      .sortBy(_.level)
  }

}

case class AccessibilityResult(level : String, standard : String, element : String, identifier : String, description : String, context : String)

object AccessibilityResult {
  def makeTable(data : Seq[AccessibilityResult]) : String = {
    var output = new StringBuilder("""
                                     | <table style="margin-top: -4em">
                                     |   <thead>
                                     |     <tr>
                                     |       <th>Level</th>
                                     |       <th>Standard</th>
                                     |       <th>Element</th>
                                     |       <th>Detail</th>
                                     |       <th>Description</th>
                                     |       <th>Context</th>
                                     |     </tr>
                                     |   </thead>
                                     |   <tbody>
                                     |""".stripMargin)
    data.foreach(row => {
      output ++= s"""
                    |<tr>
                    | <td>${row.level}</td>
                    | <td>${row.standard}</td>
                    | <td>${row.element}</td>
                    | <td>${row.identifier}</td>
                    | <td>${row.description}</td>
                    | <td>${row.context}</td>
                    |</tr>
         """.stripMargin
    })
    output ++= "</tbody></table>"
    output.toString()
  }

  def makeScenarioSummary(scenarioResults : Seq[AccessibilityResult]) : String = {
    var output = new StringBuilder("""<h3>Summary</h3><p style="margin-top: -1em">There """)
    val grouped = scenarioResults.groupBy(_.level)
    (grouped.getOrElse("ERROR", Seq.empty).size, grouped.getOrElse("WARNING", Seq.empty).size) match {
      case (0, 0) => output ++= "were no errors or warnings"
      case (1, 0) => output ++= "was 1 error and no warnings"
      case (0, 1) => output ++= "were no errors and 1 warning"
      case (e, 0) => output ++= s"were $e errors and no warnings"
      case (0, w) => output ++= s"were no errors and $w warnings"
      case (e, w) => output ++= s"were $e errors and $w warnings"
    }
    output ++= "</p>"
    output.toString()
  }
}

object AccessibilityFilters {
  def headerFooterFilter : PartialFunction[AccessibilityResult, Boolean] = {
    case AccessibilityResult("ERROR","WCAG2AA.Principle1.Guideline1_4.1_4_3.G18.Fail","strong",_,_,"&#x003C;strong class=\"phase-tag\">...&#x003C;/strong>") => false
    case AccessibilityResult("ERROR","WCAG2AA.Principle4.Guideline4_1.4_1_2.H91.InputEmail.Name","input","#report-email",_,"&#x003C;input id=\"report-email\" maxlength=\"255\" class=\"input--fullwidth form-control\" name=\"report-email\" type=\"email\" data-rule-required=\"true\" data-rule-email=\"true\" data-msg-required=\"Please provide your email address.\" aria-required=\"true\">") => false
    case r @ AccessibilityResult("WARNING","WCAG2AA.Principle1.Guideline1_1.1_1_1.H67.2","img",_,_,_) if r.context.contains("template/assets/images/gov.uk_logotype_crown.png\" alt=\"\">") => false
    case AccessibilityResult("WARNING","WCAG2AA.Principle1.Guideline1_4.1_4_3.G18.BgImage","span",_,_,"&#x003C;span class=\"organisation-logo organisation-logo-medium\">...&#x003C;/span>") => false
    case AccessibilityResult("WARNING","WCAG2AA.Principle1.Guideline1_4.1_4_3.G18.BgImage","a",_,_,"&#x003C;a href=\"http://www.nationalarchives.gov.uk/information-management/our-services/crown-copyright.htm\" target=\"_blank\">...&#x003C;/a>") => false
  }

  def webChatFilter : PartialFunction[AccessibilityResult, Boolean] = {
    case AccessibilityResult("ERROR","WCAG2AA.Principle2.Guideline2_4.2_4_1.H64.1","iframe","#egot_iframe",_,_) => false
  }

  def emptyFilter : PartialFunction[AccessibilityResult, Boolean] = {
    case _ => true
  }

  def defaultFilter : PartialFunction[AccessibilityResult, Boolean] = headerFooterFilter orElse webChatFilter orElse emptyFilter
}

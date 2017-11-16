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

import sbt.Keys._
import sbt._
import uk.gov.hmrc.versioning.SbtGitVersioning

object HmrcBuild extends Build {

  import uk.gov.hmrc._

  val appName = "accessibility-testing-library"

  val hmrcRepoHost = java.lang.System.getProperty("hmrc.repo.host", "https://nexus-preview.tax.service.gov.uk")

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      scalaVersion := "2.11.11",
      libraryDependencies ++= AppDependencies(),
      crossScalaVersions := Seq("2.11.7"),
      resolvers ++= Seq(
        "hmrc-snapshots" at hmrcRepoHost + "/content/repositories/hmrc-snapshots",
        "hmrc-releases" at hmrcRepoHost + "/content/repositories/hmrc-releases",
        "hmrc-releases-bintray" at "https://hmrc.bintray.com/releases/")
    )
    .settings(scoverageSettings)

  lazy val scoverageSettings = {
    import scoverage._
    import scoverage.ScoverageSbtPlugin._
    Seq(
      // Semicolon-separated list of regexs matching classes to exclude
      ScoverageKeys.coverageExcludedPackages := ".*BuildInfo",
      ScoverageKeys.coverageMinimum := 80,
      ScoverageKeys.coverageFailOnMinimum := false,
      ScoverageKeys.coverageHighlighting := true
    )
  }
}

private object AppDependencies {

  lazy val test = Seq(
    "org.seleniumhq.selenium" % "selenium-java"%"2.53.0",
    "org.scalatest" % "scalatest_2.11" % "2.2.1",
    "org.mockito" % "mockito-all" % "1.10.19" % "test",
    "org.pegdown" % "pegdown" % "1.6.0",
    "info.cukes" % "cucumber-scala_2.11" % "1.2.2",
    "info.cukes" % "cucumber-java" % "1.2.2",
    "junit" % "junit" % "4.11" % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test",
    "org.jsoup" % "jsoup" % "1.10.3" % "test",
    "nu.validator" % "validator" % "17.11.1",
    "com.typesafe.play" % "play-json_2.11" % "2.6.7",
    "nu.validator" % "validator" % "17.11.1"
  )

  def apply() : Seq[ModuleID] = test
}
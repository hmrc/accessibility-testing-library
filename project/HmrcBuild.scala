/*
 * Copyright 2016 HM Revenue & Customs
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
        "typesafe-releases" at hmrcRepoHost + "/content/repositories/typesafe-releases",
        "hmrc-releases-bintray" at "https://hmrc.bintray.com/releases/")
    )
}

private object AppDependencies {

  val jerseyVersion = "1.19.3"

  import play.core.PlayVersion

  val compile = Seq(
    "com.typesafe.play" %% "play" % PlayVersion.current,
    "org.scalatest" %% "scalatest" % "3.0.3",
    "com.sun.jersey" % "jersey-client" % jerseyVersion,
    "com.sun.jersey" % "jersey-core" % jerseyVersion,
    "org.pegdown" % "pegdown" % "1.6.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.typesafe.play" %% "play-specs2" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % "1.10.19" % "test",
      "uk.gov.hmrc"%%"scala-webdriver"%"5.4.0",
      "org.seleniumhq.selenium"%"selenium-firefox-driver"%"2.53.0",
      "org.seleniumhq.selenium"%"selenium-java"%"2.53.0",
      "org.seleniumhq.selenium"%"selenium-htmlunit-driver"%"2.52.0",
      "uk.gov.hmrc"%"time_2.11"%"1.1.0",
      "com.typesafe.play" %% "play-json" % "2.3.0",
      "org.scalatest"%"scalatest_2.11"%"2.2.1",
      "org.pegdown"%"pegdown"%"1.1.0"%"test",
      "info.cukes"%"cucumber-scala_2.11"%"1.2.2",
      "info.cukes"%"cucumber-junit"%"1.2.2",
      "info.cukes"%"cucumber-picocontainer"%"1.2.2",
      "junit"%"junit"%"4.11"%"test",
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "org.scalaj"%%"scalaj-http"%"0.3.16"
      )
    }.test
  }



  def apply() = compile ++ Test()
}
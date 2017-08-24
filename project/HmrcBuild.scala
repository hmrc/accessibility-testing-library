
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
}

private object AppDependencies {

  lazy val test = Seq(
    "org.seleniumhq.selenium" % "selenium-java"%"2.53.0",
    "org.scalatest" % "scalatest_2.11" % "2.2.1",
    "info.cukes" % "cucumber-scala_2.11" % "1.2.2",
    "info.cukes" % "cucumber-java" % "1.2.2",
    "junit" % "junit" % "4.11" % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test"
  )

  def apply() : Seq[ModuleID] = test
}
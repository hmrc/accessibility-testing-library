val appName = "accessibility-testing-library"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(
    scalaVersion        := "2.11.11",
    libraryDependencies ++= LibDependencies.test,
    crossScalaVersions  := Seq("2.11.7"),
    resolvers           ++= Seq("hmrc-releases-bintray" at "https://hmrc.bintray.com/releases/")
  )
  .settings(scoverageSettings)

lazy val scoverageSettings = {
  import scoverage._
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := ".*BuildInfo",
    ScoverageKeys.coverageMinimum          := 80,
    ScoverageKeys.coverageFailOnMinimum    := false,
    ScoverageKeys.coverageHighlighting     := true
  )
}
val libName = "accessibility-testing-library"

lazy val microservice = Project(libName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    majorVersion                     := 0,
    makePublicallyAvailableOnBintray := true
  )
  .settings(
    scalaVersion        := "2.11.12",
    libraryDependencies ++= LibDependencies.test,
    crossScalaVersions  := Seq("2.11.12"),
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
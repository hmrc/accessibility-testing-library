import sbt._

object LibDependencies {

  val test = Seq(
    "org.seleniumhq.selenium" % "selenium-java"       % "2.53.0",
    "org.scalatest"           % "scalatest_2.11"      % "2.2.6",
    "org.mockito"             % "mockito-all"         % "1.10.19" % "test",
    "org.pegdown"             % "pegdown"             % "1.6.0",
    "info.cukes"              % "cucumber-scala_2.11" % "1.2.5",
    "info.cukes"              % "cucumber-java"       % "1.2.5",
    "junit"                   % "junit"               % "4.12" % "test",
    "com.novocode"            % "junit-interface"     % "0.11" % "test",
    "org.jsoup"               % "jsoup"               % "1.11.3" % "test",
    "nu.validator"            % "validator"           % "17.11.1",
    "com.typesafe.play"       % "play-json_2.11"      % "2.6.13",
    "nu.validator"            % "validator"           % "17.11.1"
  )

}

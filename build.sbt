val commonSettings = Seq(
  name := "reactive-ldap",
  scalaVersion := "2.12.2",
  version := "0.1.0-SNAPSHOT"
)

val compileOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:_"
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings: _*)
  .settings(scalacOptions ++= compileOptions)
  .settings(libraryDependencies ++= Dependencies.all)
  /*
  * Fail the build with the following(example):
  * coverageMinimum := 80
  * coverageFailOnMinimum := true
  * */
  .settings(coverageEnabled := true)

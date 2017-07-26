val unboundidSdkVersion = "4.0.0"
val unboundidSdk = "com.unboundid" % "unboundid-ldapsdk" % unboundidSdkVersion

val scalaTestVersion = "3.0.3"
val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "Test"

lazy val root = (project in file(".")).
  settings(
    name := "reactive-ldap",
    scalaVersion := "2.12.2",
    libraryDependencies ++= Seq(
      unboundidSdk,
      scalaTest
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-language:_"
    )
  )
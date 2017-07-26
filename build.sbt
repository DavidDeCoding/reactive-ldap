val unboundidSdkVersion = "4.0.0"
val unboundidSdk = "com.unboundid" % "unboundid-ldapsdk" % unboundidSdkVersion

lazy val root = (project in file(".")).
  settings(
    name := "reactive-ldap",
    scalaVersion := "2.12.2",
    libraryDependencies ++= Seq(
      unboundidSdk
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-language:_"
    )
  )
import sbt._

object Dependencies {

  object Versions {
    // compile
    val unboundidSdkVersion = "4.0.0"
    val pureConfigVersion = "0.7.2"

    // test
    val scalaTestVersion = "3.0.3"
    val apacheDirectoryServiceVersion = "1.5.5"
  }

  // compile
  val unboundidSdk = "com.unboundid" % "unboundid-ldapsdk" % Versions.unboundidSdkVersion
  val pureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfigVersion

  //test
  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTestVersion % "test"
  val apacheDirectoryService = "org.apache.directory.server" % "apacheds-all" % Versions.apacheDirectoryServiceVersion % "test"

  val compile = Seq(
    unboundidSdk
  )

  val test = Seq(
    scalaTest,
    apacheDirectoryService
  )

  val all = compile ++ test
}

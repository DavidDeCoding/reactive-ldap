import sbt._

object Dependencies {

  object Versions {
    // compile
    val pureConfigVersion = "0.7.2"
    val apacheLdapSdkVersion = "1.0.0-RC2"

    // test
    val scalaTestVersion = "3.0.3"
    val unboundidSdkVersion = "4.0.0"
  }

  // compile
  val apacheLdapSdk = "org.apache.directory.api" % "api-all" % Versions.apacheLdapSdkVersion
  val pureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfigVersion

  //test
  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTestVersion % "test"
  val unboundidSdkTest = "com.unboundid" % "unboundid-ldapsdk" % Versions.unboundidSdkVersion % "test"

  val compile = Seq(
    apacheLdapSdk
  )

  val test = Seq(
    scalaTest,
    unboundidSdkTest
  )

  val all = compile ++ test
}

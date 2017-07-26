import sbt._

object Dependencies {

  object Versions {
    // compile
    val unboundidSdk = "4.0.0"
    val pureConfig = "0.7.2"

    // test
    val scalaTest = "3.0.3"
  }

  // compile
  val unboundidSdk = "com.unboundid" % "unboundid-ldapsdk" % Versions.unboundidSdk
  val pureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig

  //test
  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "Test"

  val compile = Seq(
    unboundidSdk
  )

  val test = Seq(
    scalaTest
  )

  val all = compile ++ test
}

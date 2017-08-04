package org.reactive.ldap

import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.directory.api.ldap.model.entry.DefaultEntry
import org.apache.directory.api.ldap.model.message.{AddRequestImpl, ResultCodeEnum}
import org.apache.directory.ldap.client.api.LdapConnectionConfig
import org.reactive.ldap.utils.EmbeddedLdapServer
import org.scalatest.{FreeSpecLike, Matchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

class LdapConnectionSpec
  extends FreeSpecLike
  with EmbeddedLdapServer
  with Matchers
  with ScalaFutures{

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(500, Millis))

  "LdapConnection should" - {

    var builder = LdapConnection.Builder()
    var ldapConnection: LdapConnection = null

    val ldapConfig = new LdapConnectionConfig()
    ldapConfig.setLdapHost("localhost")
    ldapConfig.setLdapPort(port)

    val ldapTimeout = 10L

    val ldapPoolConfig = new GenericObjectPool.Config()
    ldapPoolConfig.lifo = true
    ldapPoolConfig.maxActive = 8
    ldapPoolConfig.maxIdle = 8
    ldapPoolConfig.maxWait = -1L
    ldapPoolConfig.minEvictableIdleTimeMillis = 1000L * 60L * 30L
    ldapPoolConfig.minIdle = 0
    ldapPoolConfig.numTestsPerEvictionRun = 3
    ldapPoolConfig.softMinEvictableIdleTimeMillis = -1L
    ldapPoolConfig.testOnBorrow = false
    ldapPoolConfig.testOnReturn = false
    ldapPoolConfig.testWhileIdle = false
    ldapPoolConfig.timeBetweenEvictionRunsMillis = -1L
    ldapPoolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK

    "be able to add ldap config" in {
      builder = builder.withLdapConfig(ldapConfig)

      builder should equal(LdapConnection.Builder(Option(ldapConfig), None))
    }

    "be able to add ldap pool config" in {
      builder = builder.withLdapPoolConfig(ldapPoolConfig)

      builder should equal(LdapConnection.Builder(Option(ldapConfig), Option(ldapPoolConfig)))
    }

    "be able to create ldap connection" in {
      try { ldapConnection = builder.build() }
      catch { case ex: Throwable => fail("Should not throw exception", ex) }

    }

    "be able to add entry" in {
      val entry = new DefaultEntry(
        s"cn=testadd_cn, $testDn",
        "ObjectClass: top",
        "ObjectClass: person",
        "cn: testadd_cn",
        "sn: testadd_sn"
      )

      val addRequest = new AddRequestImpl()
      addRequest.setEntry(entry)

      whenReady(ldapConnection.add(addRequest)) { result =>
        result.get.getLdapResult.getResultCode should equal(ResultCodeEnum.SUCCESS)
      }
    }
  }
}
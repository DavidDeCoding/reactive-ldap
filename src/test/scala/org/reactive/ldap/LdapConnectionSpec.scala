package org.reactive.ldap

import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.directory.api.ldap.model.entry.{DefaultEntry, DefaultModification, ModificationOperation}
import org.apache.directory.api.ldap.model.message._
import org.apache.directory.api.ldap.model.name.Dn
import org.apache.directory.ldap.client.api.LdapConnectionConfig
import org.reactive.ldap.utils.EmbeddedLdapServer
import org.scalatest.{FreeSpecLike, Matchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

import scala.collection.JavaConverters._

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

    val entry = new DefaultEntry(
      s"cn=testadd_cn, $testDn",
      "ObjectClass: top",
      "ObjectClass: person",
      "ObjectClass: inetOrgPerson",
      "ObjectClass: organizationalPerson",
      "cn: testadd_cn",
      "sn: testadd_sn"
    )

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
      val addRequest = new AddRequestImpl()
      addRequest.setEntry(entry)

      whenReady(ldapConnection.add(addRequest)) { result =>
        result.getLdapResult.getResultCode should equal(ResultCodeEnum.SUCCESS)
      }
    }

    "be able to modify entry" in {
      val modifyRequest = new ModifyRequestImpl()
      modifyRequest.setName(new Dn(s"cn=testadd_cn, $testDn"))
      modifyRequest.addModification(new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, "givenName", "John", "Peter"))

      whenReady(ldapConnection.modify(modifyRequest)) { result =>
        result.getLdapResult.getResultCode should equal(ResultCodeEnum.SUCCESS)
      }
    }

    "be able to search entry" in {
      val expectedEntry = entry.add("givenName", "John", "Peter")

      val searchRequest = new SearchRequestImpl()
      searchRequest.setBase(new Dn(testDn))
      searchRequest.setScope(SearchScope.SUBTREE)
      searchRequest.setFilter("(cn=testadd_cn)")

      whenReady(ldapConnection.search(searchRequest)) { result =>
        Option(result.asScala.head).map(_.asInstanceOf[SearchResultEntry].getEntry) match {
          case Some(entry) =>
            entry.get("givenName") should equal(expectedEntry.get("givenName"))
            entry.get("cn") should equal(expectedEntry.get("cn"))
            entry.get("sn") should equal(expectedEntry.get("sn"))
            entry.getDn should equal(expectedEntry.getDn)

          case None => fail("Missing entry!")
        }
      }
    }

    "be able to compare entry" in {
      val compareRequest = new CompareRequestImpl()
      compareRequest.setName(new Dn(s"cn=testadd_cn, $testDn"))
      compareRequest.setAttributeId(entry.get("sn").getId)
      compareRequest.setAssertionValue("testadd_sn")

      whenReady(ldapConnection.compare(compareRequest)) { result =>
        result.isTrue should equal(true)
      }
    }

    "be able to delete request" in {
      val deleteRequest = new DeleteRequestImpl()
      deleteRequest.setName(new Dn(s"cn=testadd_cn, $testDn"))

      whenReady(ldapConnection.delete(deleteRequest)) { result =>
        result.getLdapResult.getResultCode should equal(ResultCodeEnum.SUCCESS)
      }
    }
  }
}
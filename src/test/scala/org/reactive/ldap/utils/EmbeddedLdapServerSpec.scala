package org.reactive.ldap.utils

import org.apache.directory.api.ldap.model.entry.DefaultEntry
import org.apache.directory.api.ldap.model.message.SearchScope
import org.apache.directory.ldap.client.api.LdapNetworkConnection
import org.scalatest.{FreeSpecLike, Matchers}

import scala.collection.JavaConverters._

class EmbeddedLdapServerSpec
  extends FreeSpecLike
  with EmbeddedLdapServer
  with Matchers {

  /**
    * The test ldapConnection to embedded ldapServer
    */
  val ldapConnection = new LdapNetworkConnection("localhost", port)

  override def beforeAll(): Unit = {
    super.beforeAll()

    /**
      * Bind ldapConnection to embedded ldapServer
      */
    ldapConnection.bind()
  }

  "EmbeddedLdapServer should" - {

    /**
      * The test entry
      */
    val entry = new DefaultEntry(
      s"cn=testadd_cn, $testDn",
      "ObjectClass: top",
      "ObjectClass: person",
      "cn: testadd_cn",
      "sn: testadd_sn"
    )

    "be able to add entry" in {
      noException should be thrownBy ldapConnection.add(entry)
    }

    "be able to search entry" in {
      val cursor = ldapConnection.search(testDn, "(cn=testadd_cn)", SearchScope.SUBTREE)
      Option(cursor.asScala.head) should equal(Some(entry))
    }
  }
}

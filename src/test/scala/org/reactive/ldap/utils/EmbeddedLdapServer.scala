package org.reactive.ldap.utils

import java.io.File
import java.util

import scala.collection.JavaConverters._
import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfterAll, Suite}
import org.apache.directory.server.core.DefaultDirectoryService
import org.apache.directory.server.core.entry.ServerEntry
import org.apache.directory.server.core.partition.impl.btree.jdbm.{JdbmIndex, JdbmPartition}
import org.apache.directory.server.ldap.LdapServer
import org.apache.directory.server.protocol.shared.transport.TcpTransport
import org.apache.directory.server.xdbm.Index
import org.apache.directory.shared.ldap.name.LdapDN
import org.reactive.ldap.utils.EmbeddedLdapServer._

object EmbeddedLdapServer {

  /**
    *
    * @param id Id of the partition
    * @param suffixDn Suffix Dn of the partition
    * @param indexes Indexes of the partition
    * @param attributes Attributes for the entry for partition
    */
  case class Partition(
    id: String,
    suffixDn: String,
    indexes: Set[String] = Set.empty[String],
    attributes: Map[String, Set[String]] = Map.empty[String, Set[String]])

  /**
    *
    * @param id Id of the partition
    * @param suffix SuffixDn of the partition
    * @return The new partition object with id and suffix
    */
  def createPartition(id: String, suffix: String): JdbmPartition = {
    val partition = new JdbmPartition()
    partition.setId(id)
    partition.setSuffix(suffix)
    partition
  }

  /**
    *
    * @param indexes List of indexes
    * @return The set of Index objects for each index
    */
  def createIndex(indexes: Set[String]): util.Set[Index[_, ServerEntry]] = {
    indexes.foldLeft(Set.empty[Index[_, ServerEntry]]) { (indexSet, index) =>
      indexSet + new JdbmIndex[String, ServerEntry](index).asInstanceOf[Index[_, ServerEntry]]
    }.asJava
  }

  /**
    * Generate random number
    */
  def randomNumber(from: Int, to: Int): Int = {
    if (from >= to) throw new RuntimeException("Wrong number range provided.")

    val random = scala.util.Random
    from + random.nextInt(to - from)
  }

  /**
    * Generate random port
    */
  def randomPort: Int = randomNumber(10389, 11389)

  /**
    * Generate random directory
    */
  def randomDirectory: String = s"apache-ds-${randomNumber(0, 100)}"
}

/**
  * Embeddable Ldap Server for testing purposes.
  */
trait EmbeddedLdapServer extends BeforeAndAfterAll { this: Suite =>

  /**
    * The temporary working directory
    */
  lazy val workingDirectory: File = new File("java.io.tmpdir", randomDirectory)

  /**
    * The Directory Service
    */
  lazy val directoryService: DefaultDirectoryService = new DefaultDirectoryService()

  /**
    * The LdapServer wrapped around the directory service
    */
  lazy val ldapServer: LdapServer = new LdapServer()

  /**
    * The port for the ldapServer
    */
  lazy val port: Int = randomPort

  /**
    * List of partitions
    */
  lazy val partitions: List[Partition] = List(
    Partition(
      id = "test",
      suffixDn = "dc=test,dc=org",
      indexes = Set("objectClass", "ou", "uid"),
      attributes = Map(
        "objectClass" -> Set("top", "domain", "extensibleObject"),
        "dc" -> Set("apache"))))


  override def beforeAll(): Unit = {
    /**
      * Clean working directory
      */
    FileUtils.deleteDirectory(workingDirectory)


    /**
      * Initialize the working directory
      */
    directoryService.setWorkingDirectory(workingDirectory)
    directoryService.getChangeLog.setEnabled(false)


    /**
      * Add partitions for the directory service
      */
    partitions.foreach { partition =>
      val jdbmPartition = createPartition(partition.id, partition.suffixDn)
      val jdbmIndexes = createIndex(partition.indexes)
      directoryService.addPartition(jdbmPartition)
    }

    /**
      * Start the directory service
      */
    directoryService.startup()

    /**
      * Add entries
      */
    partitions.foreach { partition =>
      val entry = directoryService.newEntry(new LdapDN(partition.suffixDn))
      partition.attributes.foreach { case (attribute, value) =>
        entry.add(attribute, value.toArray:_*)
      }
      directoryService.getAdminSession.add(entry)
    }

    /**
      * Initialize ldap server with directory service
      */
    ldapServer.setDirectoryService(directoryService)

    /**
      * Initialize ldap server with port
      */
    ldapServer.setTransports(new TcpTransport(port))

    /**
      * Start ldap server
      */
    ldapServer.start()
  }

  override def afterAll(): Unit = {
    /**
      * Stop ldap server
      */
    ldapServer.stop()

    /**
      * Stop the directory service
      */
    directoryService.shutdown()
  }
}

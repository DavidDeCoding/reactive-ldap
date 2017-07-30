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
import org.apache.directory.server.xdbm.Index
import org.reactive.ldap.utils.EmbeddedLdapServer.Partition

object EmbeddedLdapServer {

  /**
    *
    * @param id Id of the partition
    * @param suffixDn Suffix Dn of the partition
    * @param indexes Indexes of the partition
    */
  case class Partition(id: String, suffixDn: String, indexes: Set[String] = Set.empty[String])
}

/**
  * Embeddable Ldap Server for testing purposes.
  */
trait EmbeddedLdapServer extends BeforeAndAfterAll { this: Suite =>

  /**
    * The temporary working directory
    */
  lazy val workingDirectory: File = new File("java.io.tmpdir", "apache-ds-0")

  /**
    * The Directory Service
    */
  lazy val directoryService: DefaultDirectoryService = new DefaultDirectoryService()

  /**
    * The LdapServer wrapped around the directory service
    */
  lazy val ldapServer: LdapServer = new LdapServer()

  /**
    * List of partitions
    */
  lazy val partitions: List[Partition] =List(
    Partition(id = "test", suffixDn = "test", indexes = Set("objectClass", "ou", "uid"))
  )



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
      * Initialize ldapServer with directory service
      */
    ldapServer.setDirectoryService(directoryService)

    /**
      * Start ldapServer
      */
    ldapServer.start()
  }

  override def afterAll(): Unit = {
    /**
      * Stop ldapserver
      */
    ldapServer.stop()

    /**
      * Stop the directory service
      */
    directoryService.shutdown()
  }

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
}

package org.reactive.ldap

import java.util.concurrent.Executors

import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.directory.api.ldap.model.cursor.SearchCursor
import org.apache.directory.api.ldap.model.message._
import org.apache.directory.ldap.client.api.{DefaultLdapConnectionFactory, DefaultPoolableLdapConnectionFactory, LdapConnectionConfig, LdapConnectionPool}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object LdapConnection {

  /**
    * Default ldap config
    */
  def DefaultLdapConnectionConfig = new LdapConnectionConfig()

  /**
    * Default Ldap Pool Config
    */
  def DefaultLdapPoolConfig = new GenericObjectPool.Config()

  /**
    *
    * @param ldapConnectionPool Ldap Connection Pool
    * @return new Ldap Connection with IO Execution Context
    */
  def apply(ldapConnectionPool: LdapConnectionPool): LdapConnection = {

    val totalPooledConnections = ldapConnectionPool.getMaxActive
    val ioExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(totalPooledConnections))

    LdapConnection(ldapConnectionPool, ioExecutionContext)
  }

  case class Builder(
    ldapConfig: Option[LdapConnectionConfig] = None,
    ldapPoolConfig: Option[GenericObjectPool.Config] = None
  ) {

    /**
      *
      * @param lc Ldap Connection Configurations
      * @return LdapConnection.Builder with Ldap Connection Configurations
      */
    def withLdapConfig(lc: LdapConnectionConfig): Builder = copy(Option(lc), ldapPoolConfig)

    /**
      *
      * @param lpc Ldap Pool Configurations
      * @return LdapConnection.Builder with Ldap Pool Configurations
      */
    def withLdapPoolConfig(lpc: GenericObjectPool.Config): Builder = copy(ldapConfig, Option(lpc))

    /**
      *
      * The factory method to build LdapConnection
      *
      * @return new LdapConnection
      */
    def build(): LdapConnection = {
      val config = ldapConfig.getOrElse(DefaultLdapConnectionConfig)
      val poolConfig = ldapPoolConfig.getOrElse(DefaultLdapPoolConfig)
      val factory = new DefaultLdapConnectionFactory(config)

      val ldapConnectionPool = new LdapConnectionPool(
        new DefaultPoolableLdapConnectionFactory(factory),
        poolConfig
      )

      LdapConnection(ldapConnectionPool)
    }
  }
}

case class LdapConnection(
  ldapConnectionPool: LdapConnectionPool,
  ioExecutionContext: ExecutionContext
) {

  /**
    *
    * Executes an add request using io executor in LDAP
    *
    * @param addRequest Ldap AddRequest Object
    * @return Future of Try of AddResponse
    */
  def add(addRequest: AddRequest): Future[Try[AddResponse]] =
    Future{
      Try(ldapConnectionPool.getConnection).map(_.add(addRequest))
    }(ioExecutionContext)

  /**
    *
    * Executes a compare request using io executor in LDAP
    *
    * @param compareRequest Ldap CompareRequest Object
    * @return Future of Try of CompareResponse
    */
  def compare(compareRequest: CompareRequest): Future[Try[CompareResponse]] =
    Future {
      Try(ldapConnectionPool.getConnection).map(_.compare(compareRequest))
    }(ioExecutionContext)

  /**
    *
    * Executes a delete request using io executor in LDAP
    *
    * @param deleteRequest Ldap DeleteRequest Object
    * @return Future of Try of DeleteResponse
    */
  def delete(deleteRequest: DeleteRequest): Future[Try[DeleteResponse]] =
    Future {
      Try(ldapConnectionPool.getConnection).map(_.delete(deleteRequest))
    }(ioExecutionContext)

  /**
    *
    * Executes a modification request using io executor in LDAP
    *
    * @param modifyRequest Ldap ModifyRequest Object
    * @return Future of Try of ModifyResponse
    */
  def modify(modifyRequest: ModifyRequest): Future[Try[ModifyResponse]] =
    Future {
      Try(ldapConnectionPool.getConnection).map(_.modify(modifyRequest))
    }(ioExecutionContext)

  /**
    *
    * Executes a search request using io executor in LDAP
    *
    * @param searchRequest Ldap SearchRequest Object
    * @return Future of Try of SearchCursor
    */
  def search(searchRequest: SearchRequest): Future[Try[SearchCursor]] =
    Future {
      Try(ldapConnectionPool.getConnection).map(_.search(searchRequest))
    }(ioExecutionContext)

  /**
    * Closes Ldap Connection Pool
    */
  def close(): Unit = {
    ldapConnectionPool.close()
  }
}

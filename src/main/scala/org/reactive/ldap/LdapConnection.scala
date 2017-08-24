package org.reactive.ldap

import java.util.concurrent.Executors
import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.directory.api.ldap.model.cursor.SearchCursor
import org.apache.directory.api.ldap.model.message._
import org.apache.directory.ldap.client.api.{DefaultLdapConnectionFactory, DefaultPoolableLdapConnectionFactory, LdapConnectionConfig, LdapConnectionPool}
import scala.concurrent.{ExecutionContext, Future}

final class LdapConnection(
  ldapConnectionPool: LdapConnectionPool,
  ioExecutionContext: ExecutionContext
) {
  implicit private val ec = ioExecutionContext

  /**
    *
    * Executes an add request using io executor in LDAP
    *
    * @param addRequest Ldap AddRequest Object
    * @return Future of Try of AddResponse
    */
  def add(addRequest: AddRequest): Future[AddResponse] =
    Future(ldapConnectionPool.getConnection.add(addRequest))

  /**
    *
    * Executes a compare request using io executor in LDAP
    *
    * @param compareRequest Ldap CompareRequest Object
    * @return Future of Try of CompareResponse
    */
  def compare(compareRequest: CompareRequest): Future[CompareResponse] =
    Future(ldapConnectionPool.getConnection.compare(compareRequest))

  /**
    *
    * Executes a delete request using io executor in LDAP
    *
    * @param deleteRequest Ldap DeleteRequest Object
    * @return Future of Try of DeleteResponse
    */
  def delete(deleteRequest: DeleteRequest): Future[DeleteResponse] =
    Future(ldapConnectionPool.getConnection.delete(deleteRequest))

  /**
    *
    * Executes a modification request using io executor in LDAP
    *
    * @param modifyRequest Ldap ModifyRequest Object
    * @return Future of Try of ModifyResponse
    */
  def modify(modifyRequest: ModifyRequest): Future[ModifyResponse] =
    Future(ldapConnectionPool.getConnection.modify(modifyRequest))

  /**
    *
    * Executes a search request using io executor in LDAP
    *
    * @param searchRequest Ldap SearchRequest Object
    * @return Future of Try of SearchCursor
    */
  def search(searchRequest: SearchRequest): Future[SearchCursor] =
    Future(ldapConnectionPool.getConnection.search(searchRequest))

  /**
    * Closes Ldap Connection Pool
    */
  @throws[Exception] //TODO: wrap the `ldapConnectionPool.close()` in try{...}catch{case t => log.warn(t)} ????
  def close(): Unit = {
      ldapConnectionPool.close()
  }

}

object LdapConnection {

  val DefaultLdapConnectionConfig = new LdapConnectionConfig()

  val DefaultLdapPoolConfig = new GenericObjectPool.Config()

  def apply(ldapConnectionPool: LdapConnectionPool): LdapConnection = {

    val totalPooledConnections = ldapConnectionPool.getMaxActive
    val ioExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(totalPooledConnections))

    new LdapConnection(ldapConnectionPool, ioExecutionContext)
  }

  final case class Builder(
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

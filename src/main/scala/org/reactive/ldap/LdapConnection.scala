package org.reactive.ldap

import java.util.concurrent.Executors

import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.directory.api.ldap.codec.api.LdapApiService
import org.apache.directory.api.ldap.codec.osgi.DefaultLdapCodecService
import org.apache.directory.api.ldap.model.cursor.SearchCursor
import org.apache.directory.api.ldap.model.message._
import org.apache.directory.ldap.client.api.{DefaultPoolableLdapConnectionFactory, LdapConnectionConfig, LdapConnectionPool}

import scala.concurrent.{ExecutionContext, Future}

object LdapConnection {

  def DefaultLdapConnectionConfig = new LdapConnectionConfig()

  def DefaultLdapPoolConfig = new GenericObjectPool.Config()

  def DefaultLdapApiService = new DefaultLdapCodecService()

  def DefaultTimeout = LdapConnectionConfig.DEFAULT_TIMEOUT

  def apply(ldapConnectionPool: LdapConnectionPool): LdapConnection = {

    val totalPooledConnections = ldapConnectionPool.getMaxActive
    val ioExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(totalPooledConnections))

    LdapConnection(ldapConnectionPool, ioExecutionContext)
  }

  case class Builder(
    ldapConfig: Option[LdapConnectionConfig],
    ldapApiService: Option[LdapApiService],
    ldapTimeout: Option[Long],
    ldapPoolConfig: Option[GenericObjectPool.Config]
  ) {

    def withLdapConfig(lc: LdapConnectionConfig): Builder = copy(Option(lc), ldapApiService, ldapTimeout, ldapPoolConfig)

    def withLdapPoolConfig(lpc: GenericObjectPool.Config): Builder = copy(ldapConfig, ldapApiService, ldapTimeout, Option(lpc))

    def withLdapApiService(las: LdapApiService): Builder = copy(ldapConfig, Option(las), ldapTimeout, ldapPoolConfig)

    def withTimeout(timeout: Long): Builder = copy(ldapConfig, ldapApiService, Option(timeout), ldapPoolConfig)

    def build(): LdapConnection = {
      val config = ldapConfig.getOrElse(DefaultLdapConnectionConfig)
      val apiService = ldapApiService.getOrElse(DefaultLdapApiService)
      val timeout = ldapTimeout.getOrElse(DefaultTimeout)
      val poolConfig = ldapPoolConfig.getOrElse(DefaultLdapPoolConfig)

      val ldapConnectionPool = new LdapConnectionPool(
        config,
        apiService,
        timeout,
        poolConfig
      )

      DefaultPoolableLdapConnectionFactory

      LdapConnection(ldapConnectionPool)
    }
  }
}

case class LdapConnection(
  ldapConnectionPool: LdapConnectionPool,
  ioExecutionContext: ExecutionContext
) {

  def add(addRequest: AddRequest): Future[AddResponse] =
    Future{
      ldapConnectionPool.getConnection.add(addRequest)
    }(ioExecutionContext)

  def compare(compareRequest: CompareRequest): Future[CompareResponse] =
    Future {
      ldapConnectionPool.getConnection.compare(compareRequest)
    }(ioExecutionContext)

  def delete(deleteRequest: DeleteRequest): Future[DeleteResponse] =
    Future {
      ldapConnectionPool.getConnection.delete(deleteRequest)
    }(ioExecutionContext)

  def modify(modifyRequest: ModifyRequest): Future[ModifyResponse] =
    Future {
      ldapConnectionPool.getConnection.modify(modifyRequest)
    }(ioExecutionContext)

  def search(searchRequest: SearchRequest): Future[SearchCursor] =
    Future {
      ldapConnectionPool.getConnection.search(searchRequest)
    }(ioExecutionContext)

  def close(): Unit = {
    ldapConnectionPool.close()
  }
}

package org.reactive.ldap

import java.util.concurrent.Executors

import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.directory.api.ldap.model.cursor.SearchCursor
import org.apache.directory.api.ldap.model.message._
import org.apache.directory.ldap.client.api.{DefaultLdapConnectionFactory, DefaultPoolableLdapConnectionFactory, LdapConnectionConfig, LdapConnectionPool}

import scala.concurrent.{ExecutionContext, Future}

object LdapConnection {

  def apply(ldapConnectionPool: LdapConnectionPool): LdapConnection = {

    val totalPooledConnections = ldapConnectionPool.getMaxActive
    val ioExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(totalPooledConnections))

    LdapConnection(ldapConnectionPool, ioExecutionContext)
  }

  case class Builder(
    ldapConfig: Option[LdapConnectionConfig],
    ldapPoolConfig: Option[GenericObjectPool.Config]
  ) {

    def withLdapConfig(lc: LdapConnectionConfig): Builder = copy(Option(lc), ldapPoolConfig)

    def withLdapPoolConfig(lpc: GenericObjectPool.Config): Builder = copy(ldapConfig, Option(lpc))

    def build(): LdapConnection = {
      val config = ldapConfig.getOrElse(Defaults.LdapConfigs)
      val poolConfig = ldapPoolConfig.getOrElse(Defaults.LdapPoolConfigs)

      val defaultLdapConnectionFactory = new DefaultLdapConnectionFactory(config)

      val ldapConnectionPool = new LdapConnectionPool(
        new DefaultPoolableLdapConnectionFactory(defaultLdapConnectionFactory),
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
}

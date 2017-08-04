package org.reactive.ldap

import java.util.concurrent.Executors

import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.directory.api.ldap.model.cursor.SearchCursor
import org.apache.directory.api.ldap.model.message._
import org.apache.directory.ldap.client.api.{DefaultLdapConnectionFactory, DefaultPoolableLdapConnectionFactory, LdapConnectionConfig, LdapConnectionPool}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object LdapConnection {

  def DefaultLdapConnectionConfig = new LdapConnectionConfig()

  def DefaultLdapPoolConfig = new GenericObjectPool.Config()

  def apply(ldapConnectionPool: LdapConnectionPool): LdapConnection = {

    val totalPooledConnections = ldapConnectionPool.getMaxActive
    val ioExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(totalPooledConnections))

    LdapConnection(ldapConnectionPool, ioExecutionContext)
  }

  case class Builder(
    ldapConfig: Option[LdapConnectionConfig] = None,
    ldapPoolConfig: Option[GenericObjectPool.Config] = None
  ) {

    def withLdapConfig(lc: LdapConnectionConfig): Builder = copy(Option(lc), ldapPoolConfig)

    def withLdapPoolConfig(lpc: GenericObjectPool.Config): Builder = copy(ldapConfig, Option(lpc))

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

  def add(addRequest: AddRequest): Future[Try[AddResponse]] =
    Future{
      Try(ldapConnectionPool.getConnection).map(_.add(addRequest))
    }(ioExecutionContext)

  def compare(compareRequest: CompareRequest): Future[Try[CompareResponse]] =
    Future {
      Try(ldapConnectionPool.getConnection).map(_.compare(compareRequest))
    }(ioExecutionContext)

  def delete(deleteRequest: DeleteRequest): Future[Try[DeleteResponse]] =
    Future {
      Try(ldapConnectionPool.getConnection).map(_.delete(deleteRequest))
    }(ioExecutionContext)

  def modify(modifyRequest: ModifyRequest): Future[Try[ModifyResponse]] =
    Future {
      Try(ldapConnectionPool.getConnection).map(_.modify(modifyRequest))
    }(ioExecutionContext)

  def search(searchRequest: SearchRequest): Future[Try[SearchCursor]] =
    Future {
      Try(ldapConnectionPool.getConnection).map(_.search(searchRequest))
    }(ioExecutionContext)

  def close(): Unit = {
    ldapConnectionPool.close()
  }
}

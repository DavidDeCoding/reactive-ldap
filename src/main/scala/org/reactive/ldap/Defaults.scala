package org.reactive.ldap

import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.directory.ldap.client.api.LdapConnectionConfig

object Defaults {

  def LdapConfigs: LdapConnectionConfig = {
    val config = new LdapConnectionConfig()

    config.setLdapHost("localhost")
    config.setLdapPort(389)
    config.setName("admin")
    config.setCredentials("admin")

    config
  }

  def LdapPoolConfigs: GenericObjectPool.Config = {
    val poolConfig = new GenericObjectPool.Config()

    poolConfig.lifo = false
    poolConfig.maxActive = 8
    poolConfig.maxIdle = 8
    poolConfig.maxWait = -1L
    poolConfig.minEvictableIdleTimeMillis = 1000L * 6L * 30L
    poolConfig.minIdle = 0
    poolConfig.numTestsPerEvictionRun = 3
    poolConfig.softMinEvictableIdleTimeMillis = -1L
    poolConfig.testOnBorrow = false
    poolConfig.testOnReturn = false
    poolConfig.testWhileIdle = false
    poolConfig.timeBetweenEvictionRunsMillis = -1L
    poolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK

    poolConfig
  }
}

package com.smart.tools.worker.config

import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

trait DataBaseConfig {

  val config = ConfigFactory.load()

  private val isProd = config.getBoolean("enviroments-is-prod")

  val db: JdbcBackend.Database = if(isProd) {
    Database.forConfig("prod-post-db")
  } else {
    Database.forConfig("dev-post-db")
  }

}

package com.smart.tools.worker.config

import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

trait DataBaseConfig {

  val config = ConfigFactory.load()

}

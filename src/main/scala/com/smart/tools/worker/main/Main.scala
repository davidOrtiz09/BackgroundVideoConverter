package com.smart.tools.worker.main

import akka.actor.ActorSystem
import com.smart.tools.worker.config.DataBaseConfig
import com.smart.tools.worker.dao.VideoNoSqlImplDAO
import com.smart.tools.worker.service.{ActorConverter, EmailServiceImpl, S3Connector}


object Main extends App with DataBaseConfig {

  val system = ActorSystem("coder-Actor")
  import system.dispatcher

  val videoDao = new VideoNoSqlImplDAO()
  val emailService = new EmailServiceImpl(config)

  val s3Connector = new S3Connector(config)

  system.actorOf(ActorConverter.props(videoDao, config,  emailService, s3Connector))
}

package com.smart.tools.worker.main

import akka.actor.ActorSystem
import com.smart.tools.worker.config.DataBaseConfig
import com.smart.tools.worker.dao.VideoSqlImplDAO
import com.smart.tools.worker.service.{ActorConverter, EmailServiceImpl}


object Main extends App with DataBaseConfig {

  val system = ActorSystem("coder-Actor")
  import system.dispatcher

  val videoDao = new VideoSqlImplDAO()
  val emailService = new EmailServiceImpl(config)

  system.actorOf(ActorConverter.props(videoDao, config, db, emailService))
}

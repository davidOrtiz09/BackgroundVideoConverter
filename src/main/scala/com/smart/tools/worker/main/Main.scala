package com.smart.tools.worker.main

import akka.actor.ActorSystem
import com.smart.tools.worker.dao.VideoSqlImplDAO
import com.smart.tools.worker.service.ConverterService

object Main extends App {
  val system = ActorSystem("coder-Actor")
  val videoDao = new VideoSqlImplDAO()
  val convertedService = new ConverterService(videoDao, system)
}

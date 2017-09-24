package com.smart.tools.worker.service

import akka.actor.{Actor, Props}
import com.smart.tools.worker.dao.VideoDAO
import com.smart.tools.worker.models.Videos
import com.smart.tools.worker.service.ActorConverter.{CreateWorker, SearchUncompleteVideos, SendEmail}
import com.smart.tools.worker.service.WorkerActor.StartVideoConversion
import com.typesafe.config.Config
import slick.jdbc.JdbcBackend
import scala.concurrent.duration._

object ActorConverter{
  case object SearchUncompleteVideos
  case class CreateWorker(video: Videos)
  case class SendEmail(correo: String, nombre: String, apellido: String, url: String)

  def props(videoDAO : VideoDAO, config: Config, db: JdbcBackend.Database, emailService: EmailService) = Props(new ActorConverter(videoDAO, config, db, emailService))
}

class ActorConverter(videoDAO : VideoDAO, config: Config, db: JdbcBackend.Database, emailService: EmailService) extends Actor {

  import context.dispatcher

  private var inProgressVideosId: Seq[Int] = Seq()
  private val schedulerTime = 2.seconds

  context.system.scheduler.schedule(1.seconds, schedulerTime, self , SearchUncompleteVideos)

  def receive: Receive = {
    case SearchUncompleteVideos => {
      println("Buscando Videos que no han convertido")

      val possibleVideo = db.run(videoDAO.findNotConvertedVideos(inProgressVideosId))

      possibleVideo.foreach {
        case Some(video) => self ! CreateWorker(video)
        case None => println("No se encontro ningun video a convertir")
      }
    }

    case CreateWorker(video) => {
      inProgressVideosId = inProgressVideosId.+:(video.id)
      val worker = context.actorOf(WorkerActor.props(config, videoDAO, db))
      worker ! StartVideoConversion(video)
    }

    case SendEmail(correo, nombre, apellido, url) => {
      emailService.sendEmail(correo, nombre, apellido, url).foreach(session => session.close())
    }
  }
}

package com.smart.tools.worker.service

import akka.actor.{Actor, ActorRef, Props}
import com.amazonaws.services.sqs.model.Message
import com.smart.tools.worker.dao.VideoDAO
import com.smart.tools.worker.main.Main.config
import com.smart.tools.worker.models.{VideoWithMsg, Videos}
import com.smart.tools.worker.service.ActorConverter.{CreateWorker, DeleteSqSMsg, SearchUncompleteVideo, SendEmail}
import com.smart.tools.worker.service.SQSConsumerActor.DeleteMsg
import com.smart.tools.worker.service.WorkerActor.StartVideoConversion
import com.typesafe.config.Config
import slick.jdbc.JdbcBackend

object ActorConverter{
  case class SearchUncompleteVideo(videoMsg: VideoWithMsg)
  case class CreateWorker(video: Videos, message: Message)
  case class SendEmail(correo: String, nombre: String, apellido: String, url: String)
  case class DeleteSqSMsg(message: Message)

  def props(videoDAO : VideoDAO, config: Config, emailService: EmailService, s3Connector: S3Connector) = Props(new ActorConverter(videoDAO, config, emailService, s3Connector))
}

class ActorConverter(videoDAO : VideoDAO, config: Config, emailService: EmailService, s3Connector: S3Connector) extends Actor {

  import context.dispatcher

  var sqsConsumer: ActorRef = _

  def receive: Receive = {
    case SearchUncompleteVideo(videoMsg) => {
      println("Buscando Videos que no han convertido")

      val possibleVideo = videoDAO.findNotConvertedVideo(videoMsg.concursoId, videoMsg.videoId)

      possibleVideo match {
        case Some(video) => {
          self ! CreateWorker(video, videoMsg.message)
        }
        case None => {
          println("No se encontro ningun video a convertir")
          self ! DeleteSqSMsg(videoMsg.message)
        }
      }
    }

    case CreateWorker(video, msg) => {

      val worker = context.actorOf(WorkerActor.props(config, videoDAO, s3Connector))
      worker ! StartVideoConversion(video, msg)
    }

    case SendEmail(correo, nombre, apellido, url) => {
      val result = emailService.sendGridEmail(correo, nombre, apellido, url)
      result.onFailure{
        case error: Exception => {
          println("Error enviando correo")
          error.printStackTrace()
        }
      }
      result
    }
    case DeleteSqSMsg(msg) => {
      sqsConsumer ! DeleteMsg(msg)
    }
  }

  override def preStart(): Unit = {
    sqsConsumer = context.actorOf(SQSConsumerActor.props(config))
  }
}

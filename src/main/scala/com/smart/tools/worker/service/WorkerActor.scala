package com.smart.tools.worker.service

import akka.actor.{Actor, PoisonPill, Props}
import com.amazonaws.services.sqs.model.Message
import com.smart.tools.worker.dao.VideoDAO
import com.smart.tools.worker.models.Videos
import com.smart.tools.worker.service.ActorConverter.{DeleteSqSMsg, SendEmail}
import com.smart.tools.worker.service.WorkerActor.StartVideoConversion
import com.typesafe.config.Config
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.{FFmpeg, FFmpegExecutor, FFprobe}
import org.joda.time.{DateTime, DateTimeZone}
import scala.concurrent.Future

object WorkerActor {
  case class StartVideoConversion(video: Videos, message: Message)

  def props(config: Config, videoDAO : VideoDAO, s3Connector: S3Connector) = Props(new WorkerActor(config, videoDAO, s3Connector))
}

class WorkerActor(config: Config, videoDAO : VideoDAO, s3Connector: S3Connector) extends Actor {

  import context.dispatcher

  private val ffmpegPath = config.getString("videos.libs.ffmpeg")
  private val ffprobePath = config.getString("videos.libs.ffprobe")

  private val nonConvertedVideoPath = config.getString("videos.path.no-converted")
  private val convertedVideoPath = config.getString("videos.path.converted")

  def receive: Receive = {
    case StartVideoConversion(video, message) => {
      println("Empezando conversion video con id : " + video.video_id)
      val mySender = sender()
      for {
         _ <- s3Connector.descargarVideo(video.video_nc)
        (videoId, fileName) <- convertVideo(video)
         convertedFile = convertedVideoPath + fileName
        _ <- s3Connector.subirVideo(fileName, convertedFile)
        _ <- Future(updateVideoState(videoId, fileName, video.url_concurso))
      } yield {
        mySender ! SendEmail(video.correo, video.nombre, video.apellido, video.url_concurso)
        mySender ! DeleteSqSMsg(message)
        self ! PoisonPill
      }
    }
  }

  private def updateVideoState(videoId: Int, fileName: String, concursoId: String) = {
    println("..................Actualizando video : " + fileName + " " + "...........")

    videoDAO.updateVideoById(concursoId,videoId, fileName)
  }

  private def convertVideo(video: Videos) : Future[(Int, String)] = {
    println("..................Convirtiendo video : " + video.video_nc + " " + "...........")

    val ffmpeg: FFmpeg = new FFmpeg(ffmpegPath)
    val ffprobe: FFprobe = new FFprobe(ffprobePath)
    val nonConvertedFile = nonConvertedVideoPath + video.video_nc
    val now = DateTime.now(DateTimeZone.UTC).getMillis().toString
    val newFileName = video.video_nc.split('.')(0) + now + ".mp4"
    val convertedFile = convertedVideoPath + newFileName
    val result = Future {
      val builder: FFmpegBuilder = new FFmpegBuilder()
        .setInput(nonConvertedFile)
        .overrideOutputFiles(true)
        .addOutput(convertedFile)
        .setFormat("mp4")
        .setAudioCodec("aac")
        .setVideoCodec("libx264")
        .done()

      val executor = new FFmpegExecutor(ffmpeg, ffprobe)
      executor.createJob(builder).run()
    }.map( _ =>
      (video.video_id, newFileName)
    )

    result.onFailure{
      case error: Exception => {
        println("Error conviertiendo videos ............")
        error.printStackTrace()
      }
    }

    result
  }
}

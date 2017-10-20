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
import slick.jdbc.JdbcBackend
import scala.concurrent.Future

object WorkerActor {
  case class StartVideoConversion(video: Videos, message: Message)

  def props(config: Config, videoDAO : VideoDAO) = Props(new WorkerActor(config, videoDAO))
}

class WorkerActor(config: Config, videoDAO : VideoDAO) extends Actor {

  import context.dispatcher

  private val ffmpegPath = config.getString("videos.libs.ffmpeg")
  private val ffprobePath = config.getString("videos.libs.ffprobe")

  private val nonConvertedVideoPath = config.getString("videos.path.no-converted")
  private val convertedVideoPath = config.getString("videos.path.converted")

  def receive: Receive = {
    case StartVideoConversion(video, message) => {
      print("Empezando conversion video con id : " + video.video_id)
      val mySender = sender()
      for {
        (videoId, contentType, fileName, fileSize) <- convertVideo(video)
        _ <- Future(updateVideoState(videoId, contentType, fileName, fileSize))
      } yield {
        mySender ! SendEmail(video.correo, video.nombre, video.apellido, video.url_concurso)
        mySender ! DeleteSqSMsg(message)
        self ! PoisonPill
      }
    }
  }

  private def updateVideoState(videoId: Int, contentType: String, fileName: String, fileSize: Int) = {
    println("..................Actualizando video : " + fileName + " " + "...........")

    videoDAO.updateVideoById("",videoId, contentType, fileName, fileSize)
  }

  private def convertVideo(video: Videos) : Future[(Int, String, String, Int)] = {
    println("..................Convirtiendo video : " + video.file_file_name + " " + "...........")

    val ffmpeg: FFmpeg = new FFmpeg(ffmpegPath)
    val ffprobe: FFprobe = new FFprobe(ffprobePath)
    val nonConvertedFile = nonConvertedVideoPath + video.file_file_name
    val now = DateTime.now(DateTimeZone.UTC).getMillis().toString
    val newFileName = video.file_file_name.split('.')(0) + now + ".mp4"
    val convertedFile = convertedVideoPath + newFileName
    Future {
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
      (video.video_id, video.file_content_type, newFileName, video.file_file_size)
    )
  }
}

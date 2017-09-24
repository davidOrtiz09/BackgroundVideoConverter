package com.smart.tools.worker.service

import java.util.Calendar
import akka.actor.ActorSystem
import com.smart.tools.worker.config.DataBaseConfig
import com.smart.tools.worker.dao.VideoDAO
import com.smart.tools.worker.models.Videos
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.{FFmpeg, FFprobe}
import scala.concurrent.Future
import net.bramp.ffmpeg.FFmpegExecutor
import org.joda.time.{DateTime, DateTimeZone}
import scala.concurrent.duration._

class ConverterService(videoDAO : VideoDAO, system: ActorSystem) extends DataBaseConfig  {

  import system.dispatcher

  private val schedulerTime = 1.seconds

  private val ffmpegPath = config.getString("videos.libs.ffmpeg")
  private val ffprobePath = config.getString("videos.libs.ffprobe")

  private val nonConvertedVideoPath = config.getString("videos.path.no-converted")
  private val convertedVideoPath = config.getString("videos.path.converted")

  system.scheduler.schedule(1.seconds, schedulerTime) {
    println("Buscando Videos que no han convertido")

    convertBackgroundVideos().onFailure {
    case err:Throwable => {println("custom error message"); err.printStackTrace()}
  }
  }


  def convertBackgroundVideos() : Future[Unit] = {

    println("Buscando Videos que no han convertido")

    val possibleVideo = db.run(videoDAO.findNotConvertedVideos())

    possibleVideo.flatMap {
      case Some(video) => executeBackgroundProccess(video)
      case None => println("No se encontro ningun video a convertir")
        Future.successful(())
    }
  }

  def executeBackgroundProccess(video: Videos): Future[Unit] = {
    for {
      (videoId, contentType, fileName, fileSize) <- convertVideo(video)
      _ <- updateVideoState(videoId, contentType, fileName, fileSize)
    } yield ()
  }

  private def convertVideo(video: Videos) : Future[(Int, String, String, Int)] = {
    println("..................Convirtiendo video : " + video.fileName + " " + "...........")

    val ffmpeg: FFmpeg = new FFmpeg(ffmpegPath)
    val ffprobe: FFprobe = new FFprobe(ffprobePath)
    val nonConvertedFile = nonConvertedVideoPath + video.fileName
    val now = DateTime.now(DateTimeZone.UTC).getMillis().toString
    val newFileName = video.fileName.split('.')(0) + now + ".mp4"
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
      (video.id, video.fileContentType, newFileName, video.fileSize)
    )
  }

  private def updateVideoState(videoId: Int, contentType: String, fileName: String, fileSize: Int) = {
    println("..................Actualizando video : " + fileName + " " + "...........")

    db.run(videoDAO.updateVideoById(videoId, contentType, fileName, fileSize))
  }

}

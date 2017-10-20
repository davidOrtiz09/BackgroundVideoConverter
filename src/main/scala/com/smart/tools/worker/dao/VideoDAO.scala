package com.smart.tools.worker.dao


import com.gu.scanamo.{Scanamo, Table}
import com.smart.tools.worker.models.Videos
import com.gu.scanamo.syntax._

trait VideoDAO {

  def findNotConvertedVideo(concursoId: String, videoId : Int): Option[Videos]
  def updateVideoById(concursoId: String, videoId: Int, contentType: String, fileName: String, fileSize: Int): Int

}
class VideoNoSqlImplDAO extends VideoDAO with DynamoConnector {

  val table = Table[Videos]("videos_by_conccurso")

  def findNotConvertedVideo(concursoId: String, videoId: Int): Option[Videos] =  {
    val result = Scanamo.exec(client)(table.get('url_concurso -> concursoId and 'video_id -> videoId))
    result.flatMap {
      case Right(video) => if (video.estado) None else Some(video)
      case Left(_) => None
    }
  }

  def updateVideoById(concursoId: String, videoId: Int, contentType: String, fileName: String, fileSize: Int): Int = {
    val result = Scanamo.exec(client)(table.update('url_concurso -> concursoId and 'video_id -> videoId, set('estado -> true)
    and set('file_converted_file_name -> fileName) and set('file_converted_content_type -> contentType)
      and set('file_converted_file_size -> fileSize)))
    result match {
      case Right(_) => 1
      case Left(_) => 0
    }
  }
}

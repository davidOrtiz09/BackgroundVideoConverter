package com.smart.tools.worker.dao


import java.time.Instant
import com.smart.tools.worker.models.Videos
import slick.driver.PostgresDriver.api._

trait VideoDAO {

  def findNotConvertedVideos(videosId : Seq[Int]): DBIO[Option[Videos]]
  def updateVideoById(videoId: Int, contentType: String, fileName: String, fileSize: Int): DBIO[Int]

}
class VideoSqlImplDAO extends VideoDAO with VideoTable {

  override def findNotConvertedVideos(videosId : Seq[Int]): DBIO[Option[Videos]] = {
    val result = videosQuery.filterNot(row => row.estado).
      filterNot(row => row.id inSetBind videosId).
      result.headOption
    result
  }

  override def updateVideoById(videoId: Int, contentType: String, fileName: String, fileSize: Int): DBIO[Int] = {
    val result = videosQuery.filter(x => x.id === videoId && !x.estado).
      map(row => (row.estado, row.fileConvertedContentType, row.fileConvertedName, row.fileConvertedSize, row.fileConvertedUpdatedAt))
    result.update((true, Some(contentType), Some(fileName), Some(fileSize), Some(Instant.now())))
  }



}

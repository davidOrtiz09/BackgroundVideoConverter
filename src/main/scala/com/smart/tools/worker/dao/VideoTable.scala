package com.smart.tools.worker.dao

import java.sql.Timestamp
import java.time.Instant
import com.smart.tools.worker.models.Videos
import slick.driver.PostgresDriver.api._

private[dao] trait VideoTable {

  val videosQuery = TableQuery[VideoTable]

  implicit val instantColumnType = MappedColumnType.base[Instant, Timestamp](Timestamp.from(_), _.toInstant)

  class VideoTable(tag: Tag) extends Table[Videos](tag, "videos") {
    def id = column[Int]("id")
    def nombre = column[String]("nombre")
    def apellido = column[String]("apellido")
    def correo = column[String]("correo")
    def estado = column[Boolean]("estado")
    def descripcion = column[String]("descripcion")
    def createdAt = column[Instant]("created_at")
    def updatedAt = column[Instant]("updated_at")
    def fileName = column[String]("file_file_name")
    def fileContentType = column[String]("file_content_type")
    def fileSize = column[Int]("file_file_size")
    def fileUpdatedAt = column[Instant]("file_updated_at")
    def concursoId = column[String]("concurso_id")
    def fileConvertedName = column[Option[String]]("file_converted_file_name")
    def fileConvertedContentType = column[Option[String]]("file_converted_content_type")
    def fileConvertedSize = column[Option[Int]]("file_converted_file_size")
    def fileConvertedUpdatedAt = column[Option[Instant]]("file_converted_updated_at")

    def * = (id, nombre, apellido, correo,
      estado, descripcion, createdAt, updatedAt, fileName, fileContentType,
      fileSize, fileUpdatedAt, concursoId, fileConvertedName,
      fileConvertedContentType, fileConvertedSize, fileConvertedUpdatedAt) <> (Videos.tupled, Videos.unapply)
  }

}

package com.smart.tools.worker.models

import java.time.Instant

case class Videos(
                   id: Int,
                   nombre: String,
                   apellido: String,
                   correo: String,
                   estado: Boolean,
                   decripcion: String,
                   createdAt: Instant,
                   updatedAt: Instant,
                   fileName: String,
                   fileContentType: String,
                   fileSize: Int,
                   fileUpdatedAt: Instant,
                   concursoId: String,
                   fileConvertedName: Option[String],
                   fileConvertedContentType: Option[String],
                   fileConvertedSize: Option[Int],
                   fileConvertedUpdatedAt: Option[Instant]
                 )

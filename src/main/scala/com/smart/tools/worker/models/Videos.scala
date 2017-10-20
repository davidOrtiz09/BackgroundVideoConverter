package com.smart.tools.worker.models

case class Videos(
                   video_id: Int,
                   nombre: String,
                   apellido: String,
                   correo: String,
                   estado: Boolean,
                   descripcion: String,
                   created_at: String,
                   updated_at: String,
                   file_file_name: String,
                   file_content_type: String,
                   file_file_size: Int,
                   file_updated_at: String,
                   url_concurso: String,
                   file_converted_file_name: Option[String],
                   file_converted_content_type: Option[String],
                   file_converted_file_size: Option[Int],
                   file_converted_updated_at: Option[String]
                 )

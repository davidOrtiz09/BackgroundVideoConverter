package com.smart.tools.worker.models

case class Videos(
                   video_id: Int,
                   nombre: String,
                   apellido: String,
                   correo: String,
                   estado: Boolean,
                   descripcion: String,
                   video_nc: String,
                   video_c: String,
                   url_concurso: String
                 )
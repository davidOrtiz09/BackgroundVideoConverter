package com.smart.tools.worker.models

import com.amazonaws.services.sqs.model.Message

case class VideoWithMsg(videoId: Int, concursoId: String ,message: Message)

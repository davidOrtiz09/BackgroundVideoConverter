package com.smart.tools.worker.service

import akka.actor.{Actor, Props}
import akka.stream.alpakka.sqs.scaladsl.SqsSource
import akka.stream.{ActorMaterializer, ThrottleMode}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sqs.model.{DeleteMessageRequest, Message}
import com.amazonaws.services.sqs.{AmazonSQSAsync, AmazonSQSAsyncClientBuilder}
import com.smart.tools.worker.models.VideoWithMsg
import com.smart.tools.worker.service.ActorConverter.SearchUncompleteVideo
import com.smart.tools.worker.service.SQSConsumerActor.DeleteMsg
import com.typesafe.config.Config
import scala.concurrent.duration._

object SQSConsumerActor {

  case class DeleteMsg(message: Message)

  def props(config: Config) = Props(new SQSConsumerActor(config))

}

class SQSConsumerActor(config: Config) extends Actor {

  private val queueUrl = config.getString("queue.url")
  private val sqsUrl = config.getString("queue.sqs_url")
  private val sqsREgion = config.getString("queue.sqs_region")
  private val maxMsg = config.getInt("queue.max_msg")
  private val timeDelay = config.getInt("queue.time_delay")

  implicit private val sqsClient: AmazonSQSAsync = AmazonSQSAsyncClientBuilder
    .standard()
    .withEndpointConfiguration(new EndpointConfiguration(sqsUrl, sqsREgion))
    .build()

  implicit private val mat = ActorMaterializer()

  SqsSource(queueUrl)
    .throttle(maxMsg, timeDelay.second, maxMsg, ThrottleMode.shaping)
    .runForeach((message) => {
      println(message.getBody)
      val videoMsg = VideoWithMsg(message.getBody.toInt, message)
     context.parent ! SearchUncompleteVideo(videoMsg)
    })

  def receive: Receive = {

    case DeleteMsg(message) => {
      sqsClient.deleteMessage(
        new DeleteMessageRequest(queueUrl, message.getReceiptHandle)
      )
      println("Mensaje eliminado")
    }

  }
}

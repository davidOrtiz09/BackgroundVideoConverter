package com.smart.tools.worker.service


import java.io.File
import com.amazonaws.ClientConfiguration
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{GetObjectRequest, ObjectMetadata}
import com.smart.tools.worker.config.SmartAWSCredentialsProvider
import com.typesafe.config.Config
import scala.concurrent.{ExecutionContext, Future}
import com.amazonaws.services.s3.model.ObjectMetadata
import java.io.ByteArrayInputStream
import org.apache.commons.io.FileUtils
class S3Connector(config: Config) {

  val bucketName = config.getString("aws.s3.bucket_name")
  val region = config.getString("aws.s3.region")

  val awsCredentialsProvider = new SmartAWSCredentialsProvider(config)
  val s3Client =
    AmazonS3ClientBuilder.standard()
      .withClientConfiguration(new ClientConfiguration().withReaper(true))
      .withCredentials(awsCredentialsProvider)
      .withRegion(region)
      .build()

  def descargarVideo(nombre: String)(implicit ex: ExecutionContext): Future[(ObjectMetadata, String)] = {
    /*println("...........Descargando video : " + nombre)
    val result = Future {
      val path = s"videos/noConvertidos/$nombre"
      val pathS3 = s"noConvertidos/$nombre"
      val s3Object = s3Client.getObject(new GetObjectRequest(bucketName, pathS3),
        new File(path)
      )

      (s3Object, path)
    }
    result.onFailure{
      case error: Exception => {
        println("Error descargando de S3 ............")
        error.printStackTrace()
      }
    }
    result*/
    val path = s"videos/noConvertidos/$nombre"
    val pathS3 = s"noConvertidos/$nombre"
    val s3Object = s3Client.getObject(new GetObjectRequest(bucketName, pathS3),
      new File(path)
    )

    Future.successful(s3Object, path)
  }

  def subirVideo(nombre: String, fileNamePath: String)(implicit ex: ExecutionContext) = {
    println("...........Subiendo video : " + nombre)
    val result = Future {
      val pathS3 = s"convertidos/$nombre"
      val fileStream = FileUtils.readFileToByteArray(new File(fileNamePath))
      val input = new ByteArrayInputStream(fileStream)
      s3Client.putObject(bucketName, pathS3, input, new ObjectMetadata)
    }
    result.onFailure{
      case error: Exception => {
        println("Error cargando a S3 ............")
        error.printStackTrace()
      }
    }
    result
  }


}

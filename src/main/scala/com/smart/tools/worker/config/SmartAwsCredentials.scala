package com.smart.tools.worker.config

import com.amazonaws.auth.{AWSCredentials, AWSCredentialsProvider}
import com.typesafe.config.Config

class SmartAwsCredentials(config: Config) extends AWSCredentials {

  override def getAWSAccessKeyId(): String = config.getString("aws.awsAccessKey")

  override def getAWSSecretKey(): String = config.getString("aws.awsSecretKey")
}

class SmartAWSCredentialsProvider(config: Config) extends AWSCredentialsProvider {

  val awsCredentials = new SmartAwsCredentials(config)

  override def getCredentials(): AWSCredentials = awsCredentials

  override def refresh() = {
  }
}

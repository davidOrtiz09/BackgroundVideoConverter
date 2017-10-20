package com.smart.tools.worker.dao

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder

trait DynamoConnector {

 val clientBuild = AmazonDynamoDBAsyncClientBuilder.standard()
 clientBuild.setRegion("us-east-1")
 val client = clientBuild.build()

}


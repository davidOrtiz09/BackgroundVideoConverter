
name := "BackgroundVideoConverter"

version := "1.0"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.zaxxer" % "HikariCP" % "2.4.1",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.amazonaws" % "aws-java-sdk" % "1.11.200",
  "net.bramp.ffmpeg" % "ffmpeg" % "0.6.1",
  "com.typesafe.akka" %% "akka-actor" % "2.4.14",
  "javax.mail" % "mail" % "1.4.7",
  "com.lightbend.akka" % "akka-stream-alpakka-sqs_2.11" % "0.13",
  "com.gu" % "scanamo_2.11" % "1.0.0-M1",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.4" % Test
)

enablePlugins(JavaServerAppPackaging)

mainClass in assembly := Some("com.smart.tools.worker.main.Main")
assemblyJarName in assembly := "smartools-worker.jar"

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
  "com.typesafe.akka" %% "akka-actor" % "2.5.4",
  "javax.mail" % "mail" % "1.4.7",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.4" % Test
)
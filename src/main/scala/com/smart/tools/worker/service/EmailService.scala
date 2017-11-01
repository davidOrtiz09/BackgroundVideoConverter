package com.smart.tools.worker.service

import javax.mail.{Message, Session, Transport}
import com.typesafe.config.Config
import scala.concurrent.{ExecutionContext, Future}
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import com.sendgrid._

trait EmailService {

  def sendEmail(correo: String, nombre: String, apellido: String, url: String): Future[Transport]

  def sendGridEmail(correo: String, nombre: String, apellido: String, url: String): Future[Response]

}

class EmailServiceImpl(config: Config)(implicit ec: ExecutionContext) extends EmailService {

  private val fromCorreo = config.getString("correo.from")
  private val fromNombre = config.getString("correo.from-nombre")
  private val host = config.getString("correo.host")
  private val smtpUsername = config.getString("correo.smtp-username")
  private val smtpPassword = config.getString("correo.smtp-password")

  private val mainUrl = config.getString("videos.main-url")

  private val port = "465"
  private val subject = "Tu video esta listo"
  private val props = System.getProperties
  props.put("mail.transport.protocol", "smtp")
  props.put("mail.smtp.port", port)
  props.put("mail.smtp.ssl.enable", "true")
  props.put("mail.smtp.auth", "true")

  def sendGridEmail(correo: String, nombre: String, apellido: String, url: String) = {
    val newUrl = "http://" + mainUrl + "/concursos/" + url
    val from = new Email(fromCorreo)
    val to = new Email(correo)
    val content = new Content("text/html", loadBody(newUrl, nombre, apellido))
    val mail = new Mail(from, subject, to, content)
    Future{
    val sg = new SendGrid(System.getenv("SENDGRID_API_KEY"))
    val request = new Request()
      println("Sending...")
      request.method = Method.POST
      request.endpoint = "mail/send"
      request.body = mail.build()
      val response: Response = sg.api(request)
      println("Email sent!")
      response
    }
  }

  def sendEmail(correo: String, nombre: String, apellido: String, url: String) = {
    val newUrl = "http://" + mainUrl + "/concursos/" + url
    val body = loadBody(newUrl, nombre, apellido)
    val (session, msg) = configEmailSettings(correo, body)
    Future {
      val transport = session.getTransport()
      println("Sending...")
      transport.connect(host, smtpUsername, smtpPassword)
      transport.sendMessage(msg, msg.getAllRecipients)
      println("Email sent!")
      transport
    }
  }

  private def configEmailSettings(toCorreo: String, body: String): (Session, MimeMessage) = {
    val session = Session.getDefaultInstance(props)

    val msg = new MimeMessage(session)
    msg.setFrom(new InternetAddress(fromCorreo, fromNombre))
    msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toCorreo))
    msg.setSubject(subject)
    msg.setContent(body, "text/html")
    (session, msg)
  }

  private def loadBody(urlConcurso: String, nombre: String, apellido: String) = {
    String.join(
      System.getProperty("line.separator"),
    "<!DOCTYPE html>",
      "<html>",
        "<head>",
         "<meta name='viewport' content='width=device-width' />",
         "<meta content='text/html; charset=UTF-8' http-equiv='Content-Type' />",
          "<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css'>",
       "</head>",
          "<body>",
            "<p style='text-align: center; align: center;'>",
              "<table cellpadding='0' style='width: 90%;border: none;background-color: #fff;margin-left: auto;margin-right: auto;padding: 2%;border-spacing: 0px;'>",
               "<tr>",
                 "<td>",
                    "<h1 style='position: relative;font-family: Arial;font-size: 1.5em;color: #003264;border-left: 3px solid #9522e2;padding-left: 20px;margin: 0;padding: 20px;'>",
                      s"Hola,$nombre $apellido</h1>",
                    "</td>",
                    "</tr>",
                    "<tr>",
                     "<td>",
                        "<p style='position: relative;font-family: Arial;color: #003264;padding-bottom: 20px;padding-top: 20px;border-left: 3px solid #00affa;padding-left: 20px;margin: 0;'>",
                         s"Gracias por la espera, tu vídeo ya ha sido procesado correctamente. </br> Si deseas verlo, solo ingresa a esta url: </br> <a href='$urlConcurso'>$urlConcurso</a> .</p>",
                    "</td>",
                  "</tr>",
                  "<tr>",
                    "<td>",
                     "<h2 style='position: relative;font-family: Arial;color: #003264;font-size: 1.3em;border-left: 3px solid #32e1e1;padding: 20px;margin: 0;'>",
                        "Saludos,<br>Smartools | Grupo 9</h2>",
                      "</td>",
                    "</tr>",
                    "<tr>",
                      "<td>",
                        "<p style='position: relative;font-family: Arial;color: grey;padding-bottom: 20px;padding-top: 20px;padding-left: 20px;margin: 0;font-size: .85em;'>",
                          "Copyright © 2017, Todos los Derechos Reservados.</br> Universidad de Los Andes.</br>",
                    "</p>",
                  "</td>",
                "</tr>",
              "</table>",
            "</body>",
          "</html>"
          )
  }

}

package com.example.gestionequipos.utils

import java.util.Properties
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender {

    fun sendEmail(report: String) {
        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com" // Cambia esto si usas otro servidor SMTP
        props["mail.smtp.port"] = "587"

        val session = Session.getInstance(props,
            object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication("tucorreo@gmail.com", "tucontraseña") // Reemplaza con tus credenciales
                }
            }
        )

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress("waltersebastianpaul@gmail.com")) // Reemplaza con tu correo electrónico
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("waltersebastianpaul@gmail.com")) // Reemplaza con el correo del destinatario
            message.subject = "Reporte de error de la aplicación"
            message.setText(report)

            Transport.send(message)

        } catch (e: MessagingException) {
            e.printStackTrace()
        }
    }
}
package me.ingannatore.dddkata.service

import org.springframework.stereotype.Service

@Service
class EmailSender {
    fun sendEmail(from: String, to: String, subject: String, message: String) {
        // implementation goes here. connect to SMTP...
        println("Pretend send email with title $subject")
    }
}

package me.ingannatore.dddkata.service

import me.ingannatore.dddkata.entity.BacklogItem
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val emailSender: EmailSender,
) {
    fun sendCongratsEmail(emails: List<String>) {
        emailSender.sendEmail(
            "happy@corp.intra",
            emails.joinToString(";"),
            "Congrats!",
            "You have finished the sprint earlier. You have more time for refactor!"
        )
    }

    fun sendNotDoneItemsDebrief(ownerEmail: String, notDoneItems: List<BacklogItem>) {
        val itemsStr: String = notDoneItems.joinToString("\n") { it.title }
        emailSender.sendEmail(
            "unhappy@corp.intra",
            ownerEmail,
            "Items not DONE",
            "The team was unable to declare 'DONE' the following items this iteration: $itemsStr"
        )
    }
}
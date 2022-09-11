package me.ingannatore.dddkata.event

import me.ingannatore.dddkata.entity.Sprint
import me.ingannatore.dddkata.repo.SprintRepository
import me.ingannatore.dddkata.service.EmailService
import me.ingannatore.dddkata.service.MailingListClient
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import javax.persistence.EntityNotFoundException

@Service
class SprintEventHandler(
    private val sprintRepository: SprintRepository,
    private val emailService: EmailService,
    private val mailingListClient: MailingListClient,
) {
    @EventListener
    fun handleSprintCompletedEvent(event: SprintCompletedEvent) {
        val sprint = getSprintById(event.sprintId)
        if (sprint.isFulfilled()) {
            println("Sending CONGRATS email to team of product " + sprint.product!!.code + ": They finished the items earlier. They have time to refactor! (OMG!)")
            val emails = mailingListClient.retrieveEmails(sprint.product!!.teamMailingList)
            emailService.sendCongratsEmail(emails)
        }
    }

    private fun getSprintById(id: Long): Sprint =
        sprintRepository
            .findById(id)
            .orElseThrow { EntityNotFoundException("No ${Sprint::class.simpleName} with id $id") }
}

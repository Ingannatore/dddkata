package me.ingannatore.dddkata.event

import me.ingannatore.dddkata.entity.Product
import me.ingannatore.dddkata.entity.Sprint
import me.ingannatore.dddkata.repo.ProductRepository
import me.ingannatore.dddkata.repo.SprintRepository
import me.ingannatore.dddkata.service.EmailService
import me.ingannatore.dddkata.service.MailingListClient
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import javax.persistence.EntityNotFoundException

@Service
class SprintEventHandler(
    private val sprintRepository: SprintRepository,
    private val productRepository: ProductRepository,
    private val emailService: EmailService,
    private val mailingListClient: MailingListClient,
) {
    @EventListener
    fun handleSprintFulfilledEvent(event: SprintFulfilledEvent) {
        val sprint = getSprintById(event.sprintId)
        val product = getProductById(sprint.productId)

        println("Sending CONGRATS email to team of product ${product.code}: They finished the items earlier. They have time to refactor! (OMG!)")

        val emails = mailingListClient.retrieveEmails(product.teamMailingList)
        emailService.sendCongratsEmail(emails)
    }

    private fun getSprintById(id: Long): Sprint =
        sprintRepository
            .findById(id)
            .orElseThrow { EntityNotFoundException("No ${Sprint::class.simpleName} with id $id") }

    private fun getProductById(id: Long): Product =
        productRepository
            .findById(id)
            .orElseThrow { EntityNotFoundException("No ${Product::class.simpleName} with id $id") }
}

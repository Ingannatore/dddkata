package me.ingannatore.dddkata.service

import me.ingannatore.dddkata.dto.AddBacklogItemRequest
import me.ingannatore.dddkata.dto.CreateSprintRequest
import me.ingannatore.dddkata.dto.LogHoursRequest
import me.ingannatore.dddkata.dto.SprintMetrics
import me.ingannatore.dddkata.entity.BacklogItem
import me.ingannatore.dddkata.entity.Product
import me.ingannatore.dddkata.entity.Sprint
import me.ingannatore.dddkata.repo.BacklogItemRepository
import me.ingannatore.dddkata.repo.ProductRepository
import me.ingannatore.dddkata.repo.SprintRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import javax.persistence.EntityNotFoundException

@Transactional
@RestController
class SprintService(
    private val sprintRepository: SprintRepository,
    private val productRepository: ProductRepository,
    private val backlogItemRepository: BacklogItemRepository,
    private val emailService: EmailService,
    private val mailingListClient: MailingListClient,
) {
    @PostMapping("sprint")
    fun createSprint(@RequestBody dto: CreateSprintRequest): Long {
        val product = productRepository.findById(dto.productId).orElseThrow { EntityNotFoundException("No ${Product::class.simpleName} with id " + dto.productId) }
        val sprint = Sprint(
            product = product,
            iteration = product.incrementAndGetIteration(),
            plannedEndDate = dto.plannedEnd,
        )

        return sprintRepository.save(sprint).id!!
    }

    @GetMapping("sprint/{sprintId}")
    fun getSprint(@PathVariable sprintId: Long): Sprint {
        return sprintRepository.findById(sprintId).orElseThrow { EntityNotFoundException("No ${Sprint::class.simpleName} with id " + sprintId) }
    }

    @PostMapping("sprint/{sprintId}/start")
    fun startSprint(@PathVariable sprintId: Long) {
        val sprint = sprintRepository.findById(sprintId).orElseThrow { EntityNotFoundException("No ${Sprint::class.simpleName} with id " + sprintId) }
        sprint.start()
    }

    @PostMapping("sprint/{sprintId}/end")
    fun endSprint(@PathVariable sprintId: Long) {
        val sprint = sprintRepository.findById(sprintId).orElseThrow { EntityNotFoundException("No ${Sprint::class.simpleName} with id " + sprintId) }
        sprint.end()
    }

    /*****************************  ITEMS IN SPRINT  */
    @PostMapping("sprint/{sprintId}/item")
    fun addItem(@PathVariable sprintId: Long, @RequestBody request: AddBacklogItemRequest): Long {
        val sprint = sprintRepository.findById(sprintId).orElseThrow { EntityNotFoundException("No ${Sprint::class.simpleName} with id " + sprintId) }
        val backlogItem = backlogItemRepository.findById(request.backlogId).orElseThrow { EntityNotFoundException("No ${BacklogItem::class.simpleName} with id " + request.backlogId) }
        return sprint.addItem(backlogItem, request.fpEstimation)
    }

    @PostMapping("sprint/{sprintId}/item/{backlogId}/start")
    fun startItem(@PathVariable sprintId: Long, @PathVariable backlogId: Long) {
        val sprint = sprintRepository.findById(sprintId).orElseThrow { EntityNotFoundException("No ${Sprint::class.simpleName} with id " + sprintId) }
        sprint.startItem(backlogId)
    }

    @PostMapping("sprint/{sprintId}/item/{backlogId}/complete")
    fun completeItem(@PathVariable sprintId: Long, @PathVariable backlogId: Long) {
        val sprint = sprintRepository.findById(sprintId).orElseThrow { EntityNotFoundException("No ${Sprint::class.simpleName} with id " + sprintId) }
        sprint.completeItem(backlogId)

        if (sprint.items.all { it.isDone() }) {
            println("Sending CONGRATS email to team of product " + sprint.product!!.code + ": They finished the items earlier. They have time to refactor! (OMG!)")
            val emails = mailingListClient.retrieveEmails(sprint.product!!.teamMailingList)
            emailService.sendCongratsEmail(emails)
        }
    }

    private fun checkSprintMatchesAndStarted(id: Long, backlogItem: BacklogItem) {
        require(backlogItem.sprint!!.id!! == id) { "item not in sprint" }
        val sprint = sprintRepository.findById(id).orElseThrow { EntityNotFoundException("No ${Sprint::class.simpleName} with id " + id) }
        check(sprint.isStarted()) { "Sprint not started" }
    }

    @PostMapping("sprint/{sprintId}/log-hours")
    fun logHours(@PathVariable sprintId: Long, @RequestBody request: LogHoursRequest) {
        val backlogItem = backlogItemRepository.findById(request.backlogId).orElseThrow { EntityNotFoundException("No ${BacklogItem::class.simpleName} with id " + request.backlogId) }
        checkSprintMatchesAndStarted(sprintId, backlogItem)
        check(backlogItem.isStarted()) { "Item not started" }
        backlogItem.addHours(request.hours)
    }

    /*****************************  METRICS  */
    @GetMapping("sprint/{sprintId}/metrics")
    fun getSprintMetrics(@PathVariable sprintId: Long): SprintMetrics {
        val sprint = sprintRepository.findById(sprintId).orElseThrow { EntityNotFoundException("No ${Sprint::class.simpleName} with id " + sprintId) }
        check(sprint.isFinished())
        val doneItems: List<BacklogItem> = sprint.items
            .filter { it.isDone() }
        val consumedHours = sprint.items.sumOf { it.hoursConsumed }
        val doneFP = doneItems.sumOf { it.fpEstimation ?: 0 }

        return SprintMetrics(
            consumedHours = consumedHours,
            calendarDays = sprint.startDate!!.until(sprint.endDate).days,
            doneFP = doneFP,
            fpVelocity = 1.0 * doneFP / consumedHours,
            hoursConsumedForNotDone = sprint.items
                .filter { !it.isDone() }
                .sumOf { it.hoursConsumed },
            delayDays = if (sprint.endDate!!.isAfter(sprint.plannedEndDate))
                sprint.plannedEndDate.until(sprint.endDate).days
            else 0
        )
    }
}

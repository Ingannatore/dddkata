package me.ingannatore.dddkata.service

import me.ingannatore.dddkata.dto.AddBacklogItemRequest
import me.ingannatore.dddkata.dto.CreateSprintRequest
import me.ingannatore.dddkata.dto.LogHoursRequest
import me.ingannatore.dddkata.dto.SprintMetrics
import me.ingannatore.dddkata.entity.BacklogItem
import me.ingannatore.dddkata.entity.Product
import me.ingannatore.dddkata.entity.Sprint
import me.ingannatore.dddkata.event.SprintFulfilledEvent
import me.ingannatore.dddkata.repo.BacklogItemRepository
import me.ingannatore.dddkata.repo.ProductRepository
import me.ingannatore.dddkata.repo.SprintRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import javax.persistence.EntityNotFoundException

@Transactional
@RestController
class SprintService(
    private val sprintRepository: SprintRepository,
    private val productRepository: ProductRepository,
    private val backlogItemRepository: BacklogItemRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @PostMapping("sprint")
    fun createSprint(@RequestBody dto: CreateSprintRequest): Long {
        val product = getProductById(dto.productId)
        val sprint = Sprint(
            product = product,
            iteration = product.incrementAndGetIteration(),
            plannedEndDate = dto.plannedEnd,
        )

        return sprintRepository.save(sprint).id!!
    }

    @GetMapping("sprint/{sprintId}")
    fun getSprint(@PathVariable sprintId: Long): Sprint {
        return getSprintById(sprintId)
    }

    @PostMapping("sprint/{sprintId}/start")
    fun startSprint(@PathVariable sprintId: Long) {
        getSprintById(sprintId).start()
    }

    @PostMapping("sprint/{sprintId}/end")
    fun endSprint(@PathVariable sprintId: Long) {
        getSprintById(sprintId).end()
    }

    /*****************************  ITEMS IN SPRINT  */
    @PostMapping("sprint/{sprintId}/item")
    fun addItem(@PathVariable sprintId: Long, @RequestBody request: AddBacklogItemRequest): Long {
        return getSprintById(sprintId).addItem(getBacklogItemById(request.backlogId), request.fpEstimation)
    }

    @PostMapping("sprint/{sprintId}/item/{backlogId}/start")
    fun startItem(@PathVariable sprintId: Long, @PathVariable backlogId: Long) {
        getSprintById(sprintId).startItem(backlogId)
    }

    @PostMapping("sprint/{sprintId}/item/{backlogId}/complete")
    fun completeItem(@PathVariable sprintId: Long, @PathVariable backlogId: Long) {
        val sprint = getSprintById(sprintId)
        sprint.completeItem(backlogId)

        if (sprint.isFulfilled()) {
            eventPublisher.publishEvent(SprintFulfilledEvent(sprintId))
        }
    }

    @PostMapping("sprint/{sprintId}/log-hours")
    fun logHours(@PathVariable sprintId: Long, @RequestBody request: LogHoursRequest) {
        getSprintById(sprintId).logHours(request.backlogId, request.hours)
    }

    /*****************************  METRICS  */
    @GetMapping("sprint/{sprintId}/metrics")
    fun getSprintMetrics(@PathVariable sprintId: Long): SprintMetrics {
        return getSprintById(sprintId).getMetrics()
    }

    private fun getProductById(id: Long): Product =
        productRepository
            .findById(id)
            .orElseThrow { EntityNotFoundException("No ${Product::class.simpleName} with id $id") }

    private fun getSprintById(id: Long): Sprint =
        sprintRepository
            .findById(id)
            .orElseThrow { EntityNotFoundException("No ${Sprint::class.simpleName} with id $id") }

    private fun getBacklogItemById(id: Long): BacklogItem =
        backlogItemRepository
            .findById(id)
            .orElseThrow { EntityNotFoundException("No ${BacklogItem::class.simpleName} with id $id") }
}

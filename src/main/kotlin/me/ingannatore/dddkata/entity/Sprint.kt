package me.ingannatore.dddkata.entity

import me.ingannatore.dddkata.dto.SprintMetrics
import java.time.LocalDate
import javax.persistence.*

@Entity
class Sprint(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @Column
    var iteration: Int = 0,

    @ManyToOne
    var product: Product? = null,

    @Column
    var startDate: LocalDate? = null,

    @Column
    var plannedEndDate: LocalDate,

    @Column
    var endDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    var status: Status = Status.CREATED,

    @OneToMany(mappedBy = "sprint")
    var items: MutableList<BacklogItem> = mutableListOf(),
) {
    enum class Status {
        CREATED, STARTED, FINISHED
    }

    fun start() {
        check(isNew())
        startDate = LocalDate.now()
        status = Status.STARTED
    }

    fun end() {
        check(isStarted())
        endDate = LocalDate.now()
        status = Status.FINISHED
    }

    fun addItem(item: BacklogItem, fpEstimation: Int): Long {
        check(isNew()) { "Can only add items to Sprint before it starts" }
        item.sprint = this
        items.add(item)
        item.fpEstimation = fpEstimation
        return item.id!!
    }

    fun startItem(itemId: Long) {
        check(isStarted()) { "Sprint not started" }
        items.first { it.id == itemId }.start()
    }

    fun logHours(itemId: Long, amount: Int) {
        check(isStarted()) { "Sprint not started" }
        items.first { it.id == itemId }.logHours(amount)
    }

    fun completeItem(itemId: Long) {
        check(isStarted()) { "Sprint not started" }
        items.first { it.id == itemId }.complete()
    }

    fun getMetrics(): SprintMetrics {
        check(isFinished())
        val totalConsumedHours = getTotalConsumedHours()
        val totalFPDone = getTotalFPDone()

        return SprintMetrics(
            consumedHours = totalConsumedHours,
            calendarDays = startDate!!.until(endDate).days,
            doneFP = totalFPDone,
            fpVelocity = 1.0 * totalFPDone / totalConsumedHours,
            hoursConsumedForNotDone = getTotalConsumedHoursOnIncompleteItems(),
            delayDays = getDelayInDays()
        )
    }

    fun isFulfilled(): Boolean = items.all { it.isDone() }
    private fun isNew(): Boolean = status == Status.CREATED
    private fun isStarted(): Boolean = status == Status.STARTED
    private fun isFinished(): Boolean = status == Status.FINISHED
    private fun getTotalConsumedHours(): Int = items.sumOf { it.hoursConsumed }
    private fun getTotalConsumedHoursOnIncompleteItems(): Int = items.filter { !it.isDone() }.sumOf { it.hoursConsumed }
    private fun getTotalFPDone(): Int = items.filter { it.isDone() }.sumOf { it.fpEstimation ?: 0 }
    private fun getDelayInDays(): Int =
        when {
            endDate!!.isAfter(plannedEndDate) -> plannedEndDate.until(endDate).days
            else -> 0
        }
}

package me.ingannatore.dddkata.entity

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

    fun isNew(): Boolean = status == Status.CREATED
    fun isStarted(): Boolean = status == Status.STARTED
    fun isFinished(): Boolean = status == Status.FINISHED

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
}

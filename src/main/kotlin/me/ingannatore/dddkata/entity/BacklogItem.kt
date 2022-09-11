package me.ingannatore.dddkata.entity

import javax.persistence.*

@Entity
class BacklogItem(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @ManyToOne
    var product: Product,

    @Column
    var title: String,

    @Column
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    var status: Status = Status.CREATED,

    @ManyToOne
    var sprint: Sprint? = null, // ⚠ not NULL when assigned to a sprint

    @Column
    var fpEstimation: Int? = null, // ⚠ not NULL when assigned to a sprint

    @Column
    var hoursConsumed: Int = 0,

    @Version
    var version: Long? = null,
) {
    enum class Status {
        CREATED, STARTED, DONE
    }

    fun isNew(): Boolean = status == BacklogItem.Status.CREATED
    fun isStarted(): Boolean = status == BacklogItem.Status.STARTED
    fun isDone(): Boolean = status == BacklogItem.Status.DONE

    fun addHours(hours: Int) {
        hoursConsumed += hours
    }
}

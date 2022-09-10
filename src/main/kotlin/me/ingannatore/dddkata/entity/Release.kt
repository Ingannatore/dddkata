package me.ingannatore.dddkata.entity

import java.time.LocalDate
import javax.persistence.*

@Entity
class Release(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @ManyToOne
    var product: Product,

    @Column
    var version: String, // eg 1.0, 2.0 ...

    @Column
    var date: LocalDate,

    @ManyToOne
    var sprint: Sprint,

    @OneToMany
    @JoinColumn
    var releasedItems: List<BacklogItem> = emptyList(), // only used for release notes
) {
    val releaseNotes: String
        get() = releasedItems.joinToString(separator = "\n") { it.title }
}

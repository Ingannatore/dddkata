package me.ingannatore.dddkata.entity

import javax.persistence.*

@Entity
class Product(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @Column
    var currentIteration: Int = 0,

    @Column
    var currentVersion: Int = 0,

    @Column
    var code: String,

    @Column
    var name: String,

    @Column
    var ownerEmail: String,

    @Column
    var ownerName: String,

    @Column
    var ownerPhone: String,

    @Column
    var teamMailingList: String,

    @OneToMany(mappedBy = "product")
    var releases: MutableList<Release> = mutableListOf(),
) {
    fun incrementAndGetIteration(): Int {
        currentIteration++
        return currentIteration
    }

    fun incrementAndGetVersion(): Int {
        currentVersion++
        return currentVersion
    }
}

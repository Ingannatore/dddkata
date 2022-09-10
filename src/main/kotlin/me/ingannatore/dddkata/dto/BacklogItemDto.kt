package me.ingannatore.dddkata.dto

data class BacklogItemDto(
    val id: Long? = null,
    val productId: Long,
    var title: String,
    var description: String? = null,
    val version: Long? = null,
)

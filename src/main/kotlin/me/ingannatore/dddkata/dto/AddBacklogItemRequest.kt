package me.ingannatore.dddkata.dto

data class AddBacklogItemRequest(
    val backlogId: Long,
    val fpEstimation: Int,
)

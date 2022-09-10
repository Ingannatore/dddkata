package me.ingannatore.dddkata.dto

data class LogHoursRequest(
    val backlogId: Long,
    val hours: Int,
)

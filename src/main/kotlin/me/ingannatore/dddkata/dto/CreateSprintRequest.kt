package me.ingannatore.dddkata.dto

import java.time.LocalDate

data class CreateSprintRequest(
    val productId: Long,
    val plannedEnd: LocalDate,
)

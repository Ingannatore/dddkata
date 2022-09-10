package me.ingannatore.dddkata.dto

data class SprintMetrics(
    val consumedHours: Int,
    val doneFP: Int,
    val fpVelocity: Double,
    val hoursConsumedForNotDone: Int,
    val calendarDays: Int,
    val delayDays: Int,
)

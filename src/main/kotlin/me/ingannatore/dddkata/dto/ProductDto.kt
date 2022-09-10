package me.ingannatore.dddkata.dto

data class ProductDto(
    val id: Long? = null,
    val code: String,
    val name: String,
    val mailingList: String,
    val poEmail: String,
    val poName: String,
    val poPhone: String,
)

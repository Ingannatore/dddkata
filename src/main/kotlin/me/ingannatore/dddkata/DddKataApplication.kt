package me.ingannatore.dddkata

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DddKataApplication

fun main(args: Array<String>) {
    runApplication<DddKataApplication>(*args)
}

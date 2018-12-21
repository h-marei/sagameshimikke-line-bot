package com.marei.sagameshimikkelinebot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SagameshimikkeLineBotApplication

fun main(args: Array<String>) {
    runApplication<SagameshimikkeLineBotApplication>(*args)
}

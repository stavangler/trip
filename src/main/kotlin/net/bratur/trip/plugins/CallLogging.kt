package net.bratur.trip.net.bratur.trip.plugins

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.request.path
import java.util.UUID
import org.slf4j.event.Level

fun Application.configureCallLogging() {
  install(CallLogging) {
    level = Level.INFO
    mdc("executionID") {
      UUID.randomUUID().toString()
    }
    filter { call -> call.request.path().startsWith("/") }
  }
}

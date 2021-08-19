package net.bratur.trip.net.bratur.trip.plugins

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson

fun Application.configureContentNegotiation() {
  install(ContentNegotiation) {
    gson { }
  }
}

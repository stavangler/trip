package net.bratur.trip.net.bratur.trip.plugins

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.locations.Locations
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import net.bratur.trip.api.trip

fun Application.configureRouting() {
  install(Locations) {
  }

  routing {
    get("/") {
      call.respondText("Hello World!")
    }
  }

  routing {
    trip()
  }
}

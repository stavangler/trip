package net.bratur.trip.api

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import mu.KotlinLogging

fun Route.trip() {
  val logger = KotlinLogging.logger {}

  route("/trip/v1") {
    get {
      logger.info { "Test /trip/v1" }
      call.respond(mapOf("hello" to "world"))
    }
  }
}

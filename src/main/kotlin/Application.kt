package net.bratur.trip

import de.nielsfalk.ktor.swagger.SwaggerSupport
import de.nielsfalk.ktor.swagger.SwaggerUiConfiguration
import de.nielsfalk.ktor.swagger.version.shared.Contact
import de.nielsfalk.ktor.swagger.version.shared.Information
import de.nielsfalk.ktor.swagger.version.v2.Swagger
import de.nielsfalk.ktor.swagger.version.v3.OpenApi
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.routing.routing
import java.util.UUID
import org.slf4j.event.Level
import net.bratur.trip.api.trip

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
  install(Authentication) { }
  install(Locations)

  install(SwaggerSupport) {
    swaggerInfo()
  }

  install(ContentNegotiation) {
    gson { }
  }

  routing {
    trip()
  }

  install(CallLogging) {
    level = Level.TRACE
    mdc("executionID") {
      UUID.randomUUID().toString()
    }
    filter { call -> call.request.path().startsWith("/") }
  }
}

private fun SwaggerUiConfiguration.swaggerInfo() {
  forwardRoot = true
  val information = Information(
    version = "1.0",
    title = "Bratur Trip API",
    description = "This is an api which combines [ktor](https://github.com/Kotlin/ktor) " +
      "with [swaggerUi](https://swagger.io/). " +
      "You find the sources on [github](https://github.com/stavangler/trip)",
    contact = Contact(
      name = "Frode Anonsen",
      email = "frode@anonsen.org",
    )
  )
  swagger = Swagger().apply {
    info = information
    // definitions["size"] = sizeSchemaMap
    // definitions[petUuid] = petIdSchema
    // ["Rectangle"] = rectangleSchemaMap("#/definitions")
  }
  openApi = OpenApi().apply {
    info = information
    // components.schemas["size"] = sizeSchemaMap
    // components.schemas[petUuid] = petIdSchema
    // components.schemas["Rectangle"] = rectangleSchemaMap("#/components/schemas")
  }
}

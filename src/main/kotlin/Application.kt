package net.bratur.trip

import io.ktor.application.Application
import io.ktor.server.netty.EngineMain
import net.bratur.trip.net.bratur.trip.plugins.configureCallLogging
import net.bratur.trip.net.bratur.trip.plugins.configureContentNegotiation
import net.bratur.trip.net.bratur.trip.plugins.configureRouting
import net.bratur.trip.net.bratur.trip.plugins.configureSecurity

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // application.conf references the main function.
fun Application.module() {
  configureRouting()
  configureContentNegotiation()
  configureCallLogging()
  configureSecurity()
}

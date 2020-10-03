import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
  kotlin("jvm") version "1.4.10"
  application
  idea
  id("com.github.johnrengelman.shadow") version "6.0.0"
  id("io.gitlab.arturbosch.detekt") version "1.14.1"
  id("org.jlleitschuh.gradle.ktlint") version "9.4.0"
  id("org.jlleitschuh.gradle.ktlint-idea") version "9.4.0"
  id("org.owasp.dependencycheck") version "6.0.2"
}
group = "stavangler.bratur.services"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  jcenter()
  maven {
    url = uri("https://dl.bintray.com/kotlin/ktor")
  }
  maven {
    url = uri("https://dl.bintray.com/kotlin/kotlinx")
  }
}
dependencies {
  testImplementation(kotlin("test-junit5"))
  implementation("io.ktor:ktor-server-netty:1.4.0")
  implementation("io.ktor:ktor-html-builder:1.4.0")
  implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")

  // logging
  implementation("net.logstash.logback:logstash-logback-encoder:6.+")
  implementation("io.github.microutils:kotlin-logging:1.7.+")
  val logbackJsonVersion = "0.1.+"
  implementation("ch.qos.logback.contrib:logback-json-classic:$logbackJsonVersion")
  implementation("ch.qos.logback.contrib:logback-jackson:$logbackJsonVersion")
}
tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "13"
}
application {
  mainClassName = "ServerKt"
}

idea {
  module {
    isDownloadJavadoc = true
    isDownloadSources = true
    outputDir = file("${project.buildDir}/classes/main")
    testOutputDir = file("${project.buildDir}/classes/test")
  }
}

detekt {
  config.setFrom(files("src/main/resources/default-detekt-config.yml"))
}

ktlint {
  version.set("0.39.0")
  outputToConsole.set(true)
  coloredOutput.set(true)
  reporters {
    reporter(ReporterType.PLAIN)
    reporter(ReporterType.CHECKSTYLE)
    reporter(ReporterType.HTML)
  }
}

dependencyCheck {
  autoUpdate = true
  format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.HTML
  outputDirectory = "build/reports/owasp"
}

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val ktor_swagger_version: String by project

plugins {
  kotlin("jvm") version "1.4.10"
  application
  idea
  jacoco
  id("project-report")
  id("com.github.johnrengelman.shadow") version "6.0.0"
  id("io.gitlab.arturbosch.detekt") version "1.14.1"
  id("org.jlleitschuh.gradle.ktlint") version "9.4.0"
  id("org.jlleitschuh.gradle.ktlint-idea") version "9.4.0"
  id("org.owasp.dependencycheck") version "6.0.2"
}

group = "net.bratur.trip"
version = "1.0-SNAPSHOT"
val dockerRepositoryName = "stavangler"
val dockerRepository = "$dockerRepositoryName.azurecr.io"
val dockerImagePrefix = "$dockerRepositoryName.azurecr.io/bratur"

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
  implementation("io.ktor:ktor-server-netty:$ktor_version")
  implementation("io.ktor:ktor-auth:$ktor_version")
  implementation("io.ktor:ktor-auth-jwt:$ktor_version")
  implementation("io.ktor:ktor-gson:$ktor_version")
  implementation("io.ktor:ktor-locations:$ktor_version")
  implementation("io.ktor:ktor-server-core:$ktor_version")
  implementation("io.ktor:ktor-server-host-common:$ktor_version")
  implementation("de.nielsfalk.ktor:ktor-swagger:$ktor_swagger_version")

  // logging
  implementation("ch.qos.logback:logback-classic:1.2.1")
  implementation("io.github.microutils:kotlin-logging:1.7.+")

  // test
  testImplementation("io.ktor:ktor-server-tests:$ktor_version")
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "13"
  }

  // Global tasks configuration
  withType<GradleBuild> {
    startParameter.showStacktrace = ShowStacktrace.ALWAYS
  }

  withType<JacocoReport> {
    reports {
      xml.apply {
        isEnabled = true
      }
      html.apply {
        isEnabled = true
      }
    }
  }

  // Always format first
  "ktlintKotlinScriptCheck" { dependsOn(":ktlintFormat") }

  withType<Test> {
    useJUnitPlatform {
      // systemProperty("spring.datasource.url", "jdbc:postgresql://localhost:45432/kotlink")
    }
    testLogging.apply {
      events("started", "passed", "skipped", "failed")
      exceptionFormat = TestExceptionFormat.SHORT
      showCauses = false
      showExceptions = false
      showStackTraces = false
      showStandardStreams = false
      info.events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }
    afterSuite(
      KotlinClosure2<TestDescriptor, TestResult, Unit>({ descriptor, result ->
        if (descriptor.parent == null) {
          logger.lifecycle(
            "Tests run: ${result.testCount}, " +
              "Failures: ${result.failedTestCount}, " +
              "Skipped: ${result.skippedTestCount}"
          )
        }
        Unit
      })
    )
  }

  register<Exec>("dockerBuild") {
    group = "build"
    description = "Build Docker image using Dockerfile in project root dir"
    doFirst { logger.lifecycle("Building docker image ${project.name}:${project.version}") }
    commandLine(
      "docker",
      "build",
      "--build-arg",
      "JAR_FILE=build/libs/${project.name}-${project.version}-all.jar",
      "-t",
      "$dockerImagePrefix/${project.name}:${project.version}",
      "."
    )
  }

  register<Exec>("dockerPush") {
    group = "publish"
    description = "Publish Docker image"
    doFirst { logger.lifecycle("Pushing docker image ${project.name}:${project.version}") }
    commandLine("docker", "push", "$dockerImagePrefix/${project.name}:${project.version}")
    dependsOn(":dockerBuild")
  }
}

application {
  mainClassName = "io.ktor.server.netty.EngineMain"
}

java {
  sourceCompatibility = JavaVersion.VERSION_13
  withJavadocJar()
  withSourcesJar()
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

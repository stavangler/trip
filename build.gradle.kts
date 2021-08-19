import org.gradle.api.logging.configuration.WarningMode
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

val logbackVersion: String by project
val ktorVersion: String by project
val kotlinVersion: String by project

plugins {
  kotlin("jvm") version "1.5.21"
  application
  idea
  jacoco
  id("project-report")
  id("com.github.johnrengelman.shadow") version "7.0.0"
  id("io.gitlab.arturbosch.detekt") version "1.18.0"
  id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
  id("org.jlleitschuh.gradle.ktlint-idea") version "10.1.0"
  id("org.owasp.dependencycheck") version "6.2.2"
}

group = "net.bratur.trip"
version = "1.0-SNAPSHOT"
val dockerRepositoryName = "stavangler"
val dockerRepository = "$dockerRepositoryName.azurecr.io"
val dockerImagePrefix = "$dockerRepositoryName.azurecr.io/bratur"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(kotlin("test-junit5"))
  implementation("io.ktor:ktor-server-netty:$ktorVersion")
  implementation("io.ktor:ktor-auth:$ktorVersion")
  implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
  implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
  implementation("io.ktor:ktor-gson:$ktorVersion")
  implementation("io.ktor:ktor-locations:$ktorVersion")
  implementation("io.ktor:ktor-server-core:$ktorVersion")
  implementation("io.ktor:ktor-server-host-common:$ktorVersion")

  // logging
  implementation("ch.qos.logback:logback-classic:$logbackVersion")
  implementation("io.github.microutils:kotlin-logging:1.7.+")

  // test
  testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
}

// Workaround for ktlint
afterEvaluate {
  arrayOf(
    "processResources",
    "ktlintMainSourceSetFormat",
    "runKtlintFormatOverMainSourceSet",
    "ktlintTestSourceSetFormat",
    "runKtlintFormatOverTestSourceSet",
    "compileKotlin",
    "ktlintKotlinScriptFormat",
    "runKtlintFormatOverKotlinScripts"
  ).forEach { name ->
    tasks.named(name).configure {
      if ("runKtlintFormatOverKotlinScripts" != name) {
        dependsOn(":runKtlintFormatOverKotlinScripts")
      }
      if (project.parent != null && project.parent?.name != rootProject.name) {
        dependsOn(":${project.parent?.name}:runKtlintFormatOverKotlinScripts")
      }
    }
  }
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
  }

  // Global tasks configuration
  withType<GradleBuild> {
    startParameter.showStacktrace = ShowStacktrace.ALWAYS
    startParameter.warningMode = WarningMode.Fail
  }

  withType<JacocoReport> {
    reports {
      xml.required.set(true)
      html.required.set(true)
      html.outputLocation.set(layout.buildDirectory.dir("${project.buildDir}/reports/jacoco/html"))
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
  mainClass.set("io.ktor.server.netty.EngineMain")
}

java {
  sourceCompatibility = JavaVersion.VERSION_16
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
  config.setFrom(files("config/detekt/default-detekt-config.yml"))
}

ktlint {
  version.set("0.42.1")
  outputToConsole.set(true)
  coloredOutput.set(true)
  baseline.set(file("config/ktlint/baseline.xml"))
  reporters {
    reporter(ReporterType.PLAIN)
    reporter(ReporterType.CHECKSTYLE)
    reporter(ReporterType.HTML)
  }
}

jacoco {
  toolVersion = "0.8.7"
  reportsDirectory.set(layout.buildDirectory.dir("${project.buildDir}/reports/jacoco"))
}

dependencyCheck {
  autoUpdate = true
  format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.HTML
  outputDirectory = "build/reports/owasp"
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    application
    id("com.github.johnrengelman.shadow") version "6.0.0"
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
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "13"
}
application {
    mainClassName = "ServerKt"
}

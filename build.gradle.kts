import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version embeddedKotlinVersion
    id("io.gitlab.arturbosch.detekt") version "1.22.0-RC2"
}

group = "org.example.detekt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
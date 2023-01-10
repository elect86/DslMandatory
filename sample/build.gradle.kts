import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.gitlab.arturbosch.detekt:detekt-api:1.20.0")

    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.20.0")
    testImplementation("io.kotest:kotest-assertions-core:5.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    detektPlugins(project(":rule"))
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    withType<Test>().configureEach {
        useJUnitPlatform()
        systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
        systemProperty("compile-snippet-tests", project.hasProperty("compile-test-snippets"))
    }
    withType<Detekt> { dependsOn(":rule:assemble") }
}
package com.github.elect86.dslmandatory

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.SourceLocation
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class DslMandatoryRuleTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `with specified mandatory parameter, does not report`() {
        val code = """
            annotation class DslLint
            annotation class DslMandatory
        
            @DslLint
            class Person (
                @DslMandatory
                var name: String = "",
                var age: Int = 0
            )

            fun person(configure: Person.() -> Unit) = Person().configure()

            fun main() {
                person {
                    name = "frodo"
                    age = 14
                }
            }
        """.trimIndent()
        val findings = DslMandatoryRule(Config.empty).compileAndLintWithContext(env, code)
        assertThat(findings).isEmpty()
    }

    @Test
    fun `with missing mandatory parameter, reports an issue`() {
        val code = """
            annotation class DslLint
            annotation class DslMandatory
        
            @DslLint
            class Person (
                @DslMandatory
                var name: String = "",
                var age: Int = 0
            )

            fun person(configure: Person.() -> Unit) = Person().configure()

            fun main() {
                person {
                    age = 14
                }
            }
        """.trimIndent()
        val findings = DslMandatoryRule(Config.empty).compileAndLintWithContext(env, code)
        assertThat(findings).hasSize(1)
        assertThat(findings).hasSourceLocation(14, 5)
        assertEquals("You declaration of 'person' is missing 1 mandatory identifiers: 'name'", findings.first().message)
    }

    @Test
    fun `with multiple mandatory parameter in the same class, reports a single issue`() {
        val code = """
            annotation class DslLint
            annotation class DslMandatory
        
            @DslLint
            class Person (
                @DslMandatory
                var name: String = "",
                @DslMandatory
                var age: Int = 0
            )

            fun person(configure: Person.() -> Unit) = Person().configure()

            fun main() {
                person {
                }
            }
        """.trimIndent()
        val findings = DslMandatoryRule(Config.empty).compileAndLintWithContext(env, code)
        assertThat(findings).hasSize(1)
        assertThat(findings).hasSourceLocation(15, 5)
        assertEquals(
            "You declaration of 'person' is missing 2 mandatory identifiers: 'name, age'",
            findings.first().message
        )
    }

    @Test
    fun `with multiple DSL invocation, reports multiple issues`() {
        val code = """
            annotation class DslLint
            annotation class DslMandatory
        
            @DslLint
            class Person (
                @DslMandatory
                var name: String = "",
                var age: Int = 0
            )

            fun person(configure: Person.() -> Unit) = Person().configure()

            fun main() {
                person {
                    age = 18
                }
                person {
                    age = 18
                }
            }
        """.trimIndent()
        val findings = DslMandatoryRule(Config.empty).compileAndLintWithContext(env, code)
        assertThat(findings).hasSize(2)
        assertThat(findings).hasSourceLocations(
            SourceLocation(14, 5),
            SourceLocation(17, 5)
        )
    }
}

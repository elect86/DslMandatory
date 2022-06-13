
annotation class DslLint
annotation class DslMandatory

@DslLint
class Person (
    @DslMandatory
    var name: String = "",
    var age: Int = 0)

fun person(configure: Person.() -> Unit) = Person().configure()

fun main() {
    person {
        age = 14
    }
}
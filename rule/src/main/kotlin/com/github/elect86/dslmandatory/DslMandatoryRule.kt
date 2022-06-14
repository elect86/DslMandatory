package com.github.elect86.dslmandatory

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.lazy.descriptors.LazyClassDescriptor

class DslMandatoryRule(config: Config) : Rule(config) {

    // Those can potentially be made configurable.
    private val lintAnnotation = FqName("DslLint")
    private val mandatoryAnnotation = FqName("DslMandatory")

    override val issue = Issue(javaClass.simpleName, Severity.CodeSmell, "Custom Rule", Debt.FIVE_MINS)

    val seenAssignedNames = mutableListOf<String>()

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        // Without Type Resolution this rule can't run. Exit early.
        if (bindingContext == BindingContext.EMPTY) return

        // Resolve types for the function call.
        val resultingDescriptor = expression.getResolvedCall(bindingContext)?.resultingDescriptor ?: return
        if (resultingDescriptor !is SimpleFunctionDescriptor) return

        // It's a simple function call, so let's check it's params
        val parameters = resultingDescriptor.valueParameters
        if (parameters.isEmpty()) return
        // Has at least one parameter. I assume the first is a lambda with receiver.
        val firstParameter = parameters.first()
        val firstParameterType = firstParameter.type
        val firstParameterTypeProjections = firstParameterType.arguments
        if (firstParameterTypeProjections.isEmpty()) return
        // The first parameter has a type reference. Let's access the receiver (the first type).
        val receiverType = firstParameterTypeProjections.first().type
        val receiverTypeConstructor = receiverType.constructor.declarationDescriptor
        if (receiverTypeConstructor is LazyClassDescriptor)
            if (receiverTypeConstructor.annotations.hasAnnotation(lintAnnotation)) {
                // Class is annotated with @DslLint
                val constructorReference = receiverTypeConstructor.constructors.first()

                // Let's collect all the constructor parameters which are annotated with @DslMandatory
                val mandatoryIdentifiers = constructorReference.valueParameters.filter {
                    it.annotations.hasAnnotation(mandatoryAnnotation)
                }.map { it.name.asString() }

                // Then we check that all the mandatory identifiers have been assigned in a binary expression.
                val missingIdentifiers = mandatoryIdentifiers.filter { it !in seenAssignedNames }
                if (missingIdentifiers.isNotEmpty())
                // If one or more haven't been assigned, we raise an issue.
                    report(CodeSmell(issue,
                                     Entity.from(expression),
                                     "You declaration of '${resultingDescriptor.name}' is missing ${missingIdentifiers.size} " +
                                             "mandatory identifiers: '${missingIdentifiers.joinToString(", ")}'")
                          )
            }
    }

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        // Whenever we visit an assignment, we save the left operand.
        expression.left?.text?.let {
            seenAssignedNames += it
        }
    }
}

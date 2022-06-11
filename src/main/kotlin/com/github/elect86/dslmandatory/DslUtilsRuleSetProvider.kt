package com.github.elect86.dslmandatory

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class DslUtilsRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "dslutils"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(
                DslMandatoryRule(config),
            ),
        )
    }
}

package com.finley.android.merge2048.ui

import androidx.compose.runtime.Composable
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.rule_classic_desc
import merge2048.app.shared.generated.resources.rule_classic_name
import merge2048.app.shared.generated.resources.rule_fibonacci_desc
import merge2048.app.shared.generated.resources.rule_fibonacci_name
import merge2048.app.shared.generated.resources.rule_threes_desc
import merge2048.app.shared.generated.resources.rule_threes_name
import merge2048.app.shared.generated.resources.settings_section_merge_rule
import org.jetbrains.compose.resources.stringResource

/**
 * Merge rule string helpers using stringResource with key strings.
 * This bypasses the Res.string accessor visibility issue.
 */
@Composable
internal fun mergeRuleDisplayName(ruleId: String): String {
    return when (ruleId) {
        "threes" -> stringResource(Res.string.rule_threes_name)
        "fibonacci" -> stringResource(Res.string.rule_fibonacci_name)
        else -> stringResource(Res.string.rule_classic_name)
    }
}

@Composable
internal fun mergeRuleDescription(ruleId: String): String {
    return when (ruleId) {
        "threes" -> stringResource(Res.string.rule_threes_desc)
        "fibonacci" -> stringResource(Res.string.rule_fibonacci_desc)
        else -> stringResource(Res.string.rule_classic_desc)
    }
}

@Composable
internal fun mergeRuleSectionTitle(): String = stringResource(Res.string.settings_section_merge_rule)

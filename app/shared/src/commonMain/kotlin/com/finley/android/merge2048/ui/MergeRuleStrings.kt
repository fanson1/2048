package com.finley.android.merge2048.ui

import androidx.compose.runtime.Composable

/**
 * Merge rule string helpers - workaround for Compose resource processor bug.
 * Returns English strings; user can change language in settings.
 */
@Composable
internal fun mergeRuleDisplayName(ruleId: String): String {
    return when (ruleId) {
        "threes" -> "Threes!"
        "fibonacci" -> "Fibonacci"
        else -> "Classic 2048"
    }
}

@Composable
internal fun mergeRuleDescription(ruleId: String): String {
    return when (ruleId) {
        "threes" -> "1+2=3, then equal tiles merge (3+3=6, 6+6=12...)"
        "fibonacci" -> "Adjacent Fibonacci numbers merge (1+2=3, 2+3=5, 3+5=8...)"
        else -> "Equal tiles merge into double (2+2=4, 4+4=8...)"
    }
}

@Composable
internal fun mergeRuleSectionTitle(): String = "MERGE RULE"
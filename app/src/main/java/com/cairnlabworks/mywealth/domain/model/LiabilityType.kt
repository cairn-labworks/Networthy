package com.cairnlabworks.mywealth.domain.model

/**
 * The category of a liability (something owed).
 */
enum class LiabilityType(val displayName: String) {
    LOAN("Loan"),
    MORTGAGE("Mortgage"),
    CREDIT_CARD("Credit card"),
    PENDING_PAYMENT("Pending payment"),
    TAX("Tax due"),
    OTHER("Other");

    companion object {
        fun fromName(name: String): LiabilityType =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}

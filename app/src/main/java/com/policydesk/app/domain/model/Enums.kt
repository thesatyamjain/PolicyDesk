package com.policydesk.app.domain.model

enum class Sector(val displayName: String) {
    PSU("PSU"),
    PRIVATE("Private")
}

enum class PolicyStatus(val displayName: String) {
    ACTIVE("Active"),
    EXPIRING_SOON("Expiring Soon"),
    OVERDUE("Overdue"),
    RENEWED("Renewed"),
    CLOSED("Closed")
}

enum class PolicySource {
    FORM,
    SPREADSHEET,
    PDF
}

enum class SaleType(val displayName: String) {
    NEW_BUSINESS("New Business"),
    RENEWAL("Renewal")
}

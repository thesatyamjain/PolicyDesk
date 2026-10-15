package com.policydesk.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

data class Customer(
    val title: String? = null,
    val name: String,
    val mobile: String,
    val email: String? = null,
    val address: String
)

data class Vehicle(
    val regNo: String,
    val customer: Customer,
    val makeModel: String? = null,
    val vin: String? = null,
    val engineNo: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isClosed: Boolean = false,
    val closeReason: String? = null
)

data class Policy(
    val id: String = UUID.randomUUID().toString(),
    val vehicleRegNo: String,
    val companySector: Sector,
    val insurer: String,
    val policyNumber: String,
    val expiryDate: Long, // Epoch ms of expiry date
    val startDate: Long? = null,
    val totalPremium: Double,
    val status: PolicyStatus = PolicyStatus.ACTIVE,
    val source: PolicySource = PolicySource.FORM,
    val attachmentId: String? = null,
    val renewedOn: Long? = null,
    val closeReason: String? = null,
    val saleType: SaleType = SaleType.NEW_BUSINESS,
    val saleDate: Long = System.currentTimeMillis(),
    val countsAsSale: Boolean = true
) {
    fun computeDaysRemaining(todayEpochDay: Long = LocalDate.now().toEpochDay()): Long {
        val expiryLocalDate = Instant.ofEpochMilli(expiryDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return ChronoUnit.DAYS.between(LocalDate.ofEpochDay(todayEpochDay), expiryLocalDate)
    }

    fun computeCurrentStatus(todayEpochDay: Long = LocalDate.now().toEpochDay()): PolicyStatus {
        if (!closeReason.isNullOrBlank()) return PolicyStatus.CLOSED
        if (renewedOn != null) return PolicyStatus.RENEWED

        val daysLeft = computeDaysRemaining(todayEpochDay)
        return when {
            daysLeft < 0 -> PolicyStatus.OVERDUE
            daysLeft <= 30 -> PolicyStatus.EXPIRING_SOON
            else -> PolicyStatus.ACTIVE
        }
    }
}

data class VehicleWithPolicies(
    val vehicle: Vehicle,
    val policies: List<Policy>
) {
    /**
     * The most recent non-closed policy is the current policy. Earlier ones are history.
     */
    val currentPolicy: Policy?
        get() = policies
            .filter { it.closeReason.isNullOrBlank() && it.renewedOn == null }
            .maxByOrNull { it.expiryDate }
            ?: policies.maxByOrNull { it.expiryDate }

    val policyHistory: List<Policy>
        get() = policies
            .filter { it.id != currentPolicy?.id }
            .sortedByDescending { it.expiryDate }
}

data class CustomerGroup(
    val mobile: String,
    val name: String,
    val title: String? = null,
    val address: String,
    val vehicles: List<VehicleWithPolicies>,
    val totalPolicies: Int,
    val totalActivePremium: Double
)

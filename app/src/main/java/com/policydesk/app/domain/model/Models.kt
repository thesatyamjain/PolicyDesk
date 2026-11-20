package com.policydesk.app.domain.model

import androidx.compose.runtime.Immutable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

@Immutable
data class Customer(
    val title: String? = null,
    val name: String,
    val mobile: String,
    val email: String? = null,
    val address: String
)

@Immutable
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
    val closeReason: String? = null,
    val plateStyle: NumberPlateStyle? = null
)

@Immutable
data class Policy(
    val id: String = UUID.randomUUID().toString(),
    val vehicleRegNo: String,
    val companySector: Sector,
    val insurer: String,
    val policyNumber: String,
    val expiryDate: Long, // Epoch ms of expiry date
    val startDate: Long? = null,
    val totalPremium: Double,
    val commissionPercentage: Double? = null,
    val commissionAmount: Double? = null,
    val status: PolicyStatus = PolicyStatus.ACTIVE,
    val source: PolicySource = PolicySource.FORM,
    val attachmentId: String? = null,
    val renewedOn: Long? = null,
    val closeReason: String? = null,
    val saleType: SaleType = SaleType.NEW_BUSINESS,
    val saleDate: Long = System.currentTimeMillis(),
    val countsAsSale: Boolean = true
) {
    val expiryLocalDate: LocalDate by lazy {
        Instant.ofEpochMilli(expiryDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    val expiryEpochDay: Long by lazy {
        expiryLocalDate.toEpochDay()
    }

    fun computeDaysRemaining(todayEpochDay: Long = LocalDate.now().toEpochDay()): Long {
        return expiryEpochDay - todayEpochDay
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

@Immutable
data class VehicleWithPolicies(
    val vehicle: Vehicle,
    val policies: List<Policy>
) {
    /**
     * The most recent non-closed policy is the current policy. Earlier ones are history.
     * Evaluated once lazily per instance for zero-allocation recomposition performance.
     */
    val currentPolicy: Policy? by lazy {
        policies
            .filter { it.closeReason.isNullOrBlank() && it.renewedOn == null }
            .maxWithOrNull(compareBy<Policy> { it.expiryDate }.thenBy { it.saleDate })
            ?: policies.maxWithOrNull(compareBy<Policy> { it.expiryDate }.thenBy { it.saleDate })
    }

    val policyHistory: List<Policy> by lazy {
        policies
            .filter { it.id != currentPolicy?.id }
            .sortedByDescending { it.expiryDate }
    }

    val earliestDate: Long by lazy {
        val firstPolicyDate = policies.minOfOrNull {
            it.startDate ?: it.saleDate.takeIf { s -> s > 0 } ?: (it.expiryDate - 365L * 24 * 60 * 60 * 1000L)
        }
        firstPolicyDate ?: vehicle.createdAt
    }

    val totalLifetimePremium: Double by lazy {
        policies.sumOf { it.totalPremium }
    }
}

@Immutable
data class CustomerGroup(
    val mobile: String,
    val name: String,
    val title: String? = null,
    val address: String,
    val vehicles: List<VehicleWithPolicies>,
    val totalPolicies: Int,
    val totalActivePremium: Double
) {
    val clientSinceDate: Long by lazy {
        vehicles.minOfOrNull { it.earliestDate } ?: System.currentTimeMillis()
    }
}

data class ExistingClientInfo(
    val clientName: String,
    val mobile: String,
    val previousInsurer: String?,
    val previousPolicyNumber: String?,
    val previousExpiryEpochMs: Long?,
    val previousPolicyId: String?,
    val totalPoliciesCount: Int = 1,
    val clientSinceEpochMs: Long? = null
)

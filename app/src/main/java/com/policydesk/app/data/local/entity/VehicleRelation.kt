package com.policydesk.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.policydesk.app.domain.model.VehicleWithPolicies

data class VehicleWithPoliciesEntity(
    @Embedded
    val vehicle: VehicleEntity,
    @Relation(
        parentColumn = "regNo",
        entityColumn = "vehicleRegNo"
    )
    val policies: List<PolicyEntity>
) {
    fun toDomain(): VehicleWithPolicies {
        return VehicleWithPolicies(
            vehicle = vehicle.toDomain(),
            policies = policies
                .filter { it.deletedAt == null }
                .map { it.toDomain() }
        )
    }
}

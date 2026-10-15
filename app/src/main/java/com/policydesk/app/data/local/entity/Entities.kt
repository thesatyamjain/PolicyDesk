package com.policydesk.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.policydesk.app.domain.model.Customer
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicySource
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.SaleType
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.domain.model.Vehicle

data class CustomerEmbedded(
    val title: String? = null,
    val name: String,
    val mobile: String,
    val email: String? = null,
    val address: String
) {
    fun toDomain() = Customer(
        title = title,
        name = name,
        mobile = mobile,
        email = email,
        address = address
    )

    companion object {
        fun fromDomain(c: Customer) = CustomerEmbedded(
            title = c.title,
            name = c.name,
            mobile = c.mobile,
            email = c.email,
            address = c.address
        )
    }
}

@Entity(
    tableName = "vehicles",
    indices = [
        Index(value = ["regNo"], unique = true),
        Index(value = ["mobile"]),
        Index(value = ["updatedAt"])
    ]
)
data class VehicleEntity(
    @PrimaryKey
    val regNo: String, // Normalized registration number
    @Embedded
    val customer: CustomerEmbedded,
    val makeModel: String? = null,
    val vin: String? = null,
    val engineNo: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isClosed: Boolean = false,
    val closeReason: String? = null,
    val deletedAt: Long? = null // For soft deletion / sync tracking
) {
    fun toDomain() = Vehicle(
        regNo = regNo,
        customer = customer.toDomain(),
        makeModel = makeModel,
        vin = vin,
        engineNo = engineNo,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isClosed = isClosed,
        closeReason = closeReason
    )

    companion object {
        fun fromDomain(v: Vehicle, deletedAt: Long? = null) = VehicleEntity(
            regNo = v.regNo,
            customer = CustomerEmbedded.fromDomain(v.customer),
            makeModel = v.makeModel,
            vin = v.vin,
            engineNo = v.engineNo,
            notes = v.notes,
            createdAt = v.createdAt,
            updatedAt = v.updatedAt,
            isClosed = v.isClosed,
            closeReason = v.closeReason,
            deletedAt = deletedAt
        )
    }
}

@Entity(
    tableName = "policies",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["regNo"],
            childColumns = ["vehicleRegNo"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["vehicleRegNo"]),
        Index(value = ["expiryDate"]),
        Index(value = ["status"]),
        Index(value = ["policyNumber"])
    ]
)
data class PolicyEntity(
    @PrimaryKey
    val id: String,
    val vehicleRegNo: String,
    val companySector: String,
    val insurer: String,
    val policyNumber: String,
    val expiryDate: Long,
    val startDate: Long? = null,
    val totalPremium: Double,
    val status: String,
    val source: String,
    val attachmentId: String? = null,
    val renewedOn: Long? = null,
    val closeReason: String? = null,
    val saleType: String,
    val saleDate: Long,
    val countsAsSale: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) {
    fun toDomain() = Policy(
        id = id,
        vehicleRegNo = vehicleRegNo,
        companySector = Sector.valueOf(companySector),
        insurer = insurer,
        policyNumber = policyNumber,
        expiryDate = expiryDate,
        startDate = startDate,
        totalPremium = totalPremium,
        status = PolicyStatus.valueOf(status),
        source = PolicySource.valueOf(source),
        attachmentId = attachmentId,
        renewedOn = renewedOn,
        closeReason = closeReason,
        saleType = SaleType.valueOf(saleType),
        saleDate = saleDate,
        countsAsSale = countsAsSale
    )

    companion object {
        fun fromDomain(p: Policy, deletedAt: Long? = null) = PolicyEntity(
            id = p.id,
            vehicleRegNo = p.vehicleRegNo,
            companySector = p.companySector.name,
            insurer = p.insurer,
            policyNumber = p.policyNumber,
            expiryDate = p.expiryDate,
            startDate = p.startDate,
            totalPremium = p.totalPremium,
            status = p.status.name,
            source = p.source.name,
            attachmentId = p.attachmentId,
            renewedOn = p.renewedOn,
            closeReason = p.closeReason,
            saleType = p.saleType.name,
            saleDate = p.saleDate,
            countsAsSale = p.countsAsSale,
            updatedAt = System.currentTimeMillis(),
            deletedAt = deletedAt
        )
    }
}

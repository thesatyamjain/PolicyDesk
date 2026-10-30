package com.policydesk.app.domain.repository

import com.policydesk.app.domain.model.DashboardSummary
import com.policydesk.app.domain.model.MonthViewData
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.Vehicle
import com.policydesk.app.domain.model.VehicleWithPolicies
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun normalizeRegistration(raw: String): String
    suspend fun exists(normalizedRegNo: String): Boolean
    suspend fun saveVehicleAndPolicy(vehicle: Vehicle, policy: Policy)
    suspend fun getVehicleWithPoliciesDirect(regNo: String): VehicleWithPolicies?
    fun getVehicleWithPolicies(regNo: String): Flow<VehicleWithPolicies?>
    fun getAllVehicles(): Flow<List<VehicleWithPolicies>>
    fun searchVehicles(query: String): Flow<List<VehicleWithPolicies>>
    fun getDistinctMakeModels(): Flow<List<String>>
    fun getDashboardSummary(): Flow<DashboardSummary>
    fun getMonthViewData(year: Int, month: Int): Flow<MonthViewData>
    fun getReportsAggregate(period: com.policydesk.app.domain.model.ReportPeriod): Flow<com.policydesk.app.domain.model.ReportsAggregate>
    suspend fun closeVehicle(regNo: String, reason: String)
    suspend fun softDeleteVehicle(regNo: String)
    suspend fun softDeleteVehicles(regNos: List<String>)
    suspend fun markPolicyRenewed(policyId: String, renewedOn: Long)
    suspend fun rollbackImport(vehicleRegNos: List<String>, policyIds: List<String>)
    fun getCustomerGroups(): Flow<List<com.policydesk.app.domain.model.CustomerGroup>>
    fun getCustomInsurers(sector: com.policydesk.app.domain.model.Sector): Flow<List<String>>
    suspend fun restoreVehiclesAndPolicies(vehiclesWithPolicies: List<VehicleWithPolicies>): Int
    suspend fun restoreSoftDeletedRecords(): Int
}

package com.policydesk.app.data.repository

import com.policydesk.app.data.local.dao.VehicleDao
import com.policydesk.app.data.local.entity.PolicyEntity
import com.policydesk.app.data.local.entity.VehicleEntity
import com.policydesk.app.domain.model.DashboardSummary
import com.policydesk.app.domain.model.MonthViewData
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.Vehicle
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import android.content.Context
import com.policydesk.app.data.drive.GoogleDriveManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao,
    @ApplicationContext private val context: Context
) : VehicleRepository {

    private fun triggerSilentDriveSync() {
        if (GoogleDriveManager.getConnectedAccount(context) != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val all = vehicleDao.getAllVehicles().first().map { it.toDomain() }
                    // Safety check: Never silently overwrite an existing backup on Drive with 0 records
                    if (all.isEmpty()) {
                        return@launch
                    }
                    val json = GoogleDriveManager.createSnapshotJson(all)
                    GoogleDriveManager.performAutoSync(context, json)
                } catch (_: Exception) {}
            }
        }
    }

    override fun normalizeRegistration(raw: String): String {
        return RegistrationNormalizer.normalize(raw)
    }

    override suspend fun exists(normalizedRegNo: String): Boolean {
        return vehicleDao.exists(normalizedRegNo)
    }

    override suspend fun saveVehicleAndPolicy(vehicle: Vehicle, policy: Policy) {
        val normalized = normalizeRegistration(vehicle.regNo)
        val now = System.currentTimeMillis()
        val vehicleWithNorm = vehicle.copy(
            regNo = normalized,
            updatedAt = now
        )
        val policyWithNorm = policy.copy(
            vehicleRegNo = normalized
        )
        vehicleDao.saveVehicleWithPolicy(
            VehicleEntity.fromDomain(vehicleWithNorm),
            PolicyEntity.fromDomain(policyWithNorm)
        )
        triggerSilentDriveSync()
    }

    override suspend fun getVehicleWithPoliciesDirect(regNo: String): VehicleWithPolicies? {
        val normalized = normalizeRegistration(regNo)
        return vehicleDao.getVehicleWithPoliciesDirect(normalized)?.toDomain()
    }

    override fun getVehicleWithPolicies(regNo: String): Flow<VehicleWithPolicies?> {
        val normalized = normalizeRegistration(regNo)
        return vehicleDao.getVehicleWithPolicies(normalized).map { it?.toDomain() }
    }

    override fun getAllVehicles(): Flow<List<VehicleWithPolicies>> {
        return vehicleDao.getAllVehicles().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun searchVehicles(query: String): Flow<List<VehicleWithPolicies>> {
        val cleanQuery = query.trim()
        val normalizedQuery = normalizeRegistration(cleanQuery)
        // Search by both raw query (for names/mobiles) and normalized query (for reg numbers)
        return vehicleDao.searchVehicles(cleanQuery).map { list ->
            val domainList = list.map { it.toDomain() }
            if (normalizedQuery.isNotEmpty() && normalizedQuery != cleanQuery) {
                domainList.sortedByDescending { it.vehicle.regNo.contains(normalizedQuery) }
            } else {
                domainList
            }
        }
    }

    override fun getDistinctMakeModels(): Flow<List<String>> {
        return vehicleDao.getDistinctMakeModels()
    }

    override fun getDashboardSummary(): Flow<DashboardSummary> {
        return getAllVehicles().map { all ->
            var overdue = 0
            var next7 = 0
            var next30 = 0
            var totalActive = 0

            val activeVehicles = mutableListOf<VehicleWithPolicies>()

            val today = LocalDate.now().toEpochDay()

            for (item in all) {
                if (item.vehicle.isClosed) continue
                val policy = item.currentPolicy ?: continue

                val daysLeft = policy.computeDaysRemaining(today)
                when {
                    daysLeft < 0 -> {
                        overdue++
                    }
                    daysLeft in 0..7 -> {
                        next7++
                        totalActive++
                    }
                    daysLeft in 8..30 -> {
                        next30++
                        totalActive++
                    }
                    else -> {
                        totalActive++
                    }
                }
                activeVehicles.add(item)
            }

            val sortedSoonest = activeVehicles.sortedBy { item ->
                item.currentPolicy?.expiryDate ?: Long.MAX_VALUE
            }

            DashboardSummary(
                overdueCount = overdue,
                next7DaysCount = next7,
                next30DaysCount = next30,
                totalActiveCount = totalActive,
                soonestExpiring = sortedSoonest
            )
        }
    }

    override fun getMonthViewData(year: Int, month: Int): Flow<MonthViewData> {
        return getAllVehicles().map { all ->
            val matchingItems = mutableListOf<VehicleWithPolicies>()
            var totalPremium = 0.0

            val zone = ZoneId.systemDefault()

            for (item in all) {
                val policy = item.currentPolicy ?: continue
                val localDate = Instant.ofEpochMilli(policy.expiryDate).atZone(zone).toLocalDate()
                if (localDate.year == year && localDate.monthValue == month) {
                    matchingItems.add(item)
                    totalPremium += policy.totalPremium
                }
            }

            val grouped = matchingItems
                .groupBy { item ->
                    val p = item.currentPolicy!!
                    Instant.ofEpochMilli(p.expiryDate).atZone(zone).toLocalDate().dayOfMonth
                }
                .toSortedMap()

            MonthViewData(
                year = year,
                month = month,
                totalPolicies = matchingItems.size,
                totalPremium = totalPremium,
                dateGroupedPolicies = grouped
            )
        }
    }

    override fun getReportsAggregate(period: com.policydesk.app.domain.model.ReportPeriod): Flow<com.policydesk.app.domain.model.ReportsAggregate> {
        val (startMillis, endMillis) = period.getStartAndEndMillis()
        val now = System.currentTimeMillis()
        val todayEpochDay = LocalDate.now().toEpochDay()
        val zone = ZoneId.systemDefault()

        return getAllVehicles().map { allVehicles ->
            // 1. Sales Calculation
            val soldItems = mutableListOf<VehicleWithPolicies>()
            var salesTotalPremium = 0.0
            var newBusinessCount = 0
            var renewalCount = 0
            val insurerSplit = mutableMapOf<String, Double>()
            val sectorSplit = mutableMapOf<com.policydesk.app.domain.model.Sector, Double>()

            // 2. Missed Renewals
            val missedAll = mutableListOf<VehicleWithPolicies>()
            val upTo30 = mutableListOf<VehicleWithPolicies>()
            val from31To90 = mutableListOf<VehicleWithPolicies>()
            val over90 = mutableListOf<VehicleWithPolicies>()
            var premiumAtRisk = 0.0

            // 3. Performance tracking
            var dueInPeriod = 0
            var onTimeInPeriod = 0
            var lateInPeriod = 0
            var missedInPeriod = 0

            // 4. Pipeline & Book
            var activeBookVehicles = 0
            var activeBookPremium = 0.0
            var pipe3Count = 0; var pipe3Prem = 0.0
            var pipe6Count = 0; var pipe6Prem = 0.0
            var pipe12Count = 0; var pipe12Prem = 0.0

            val threeMonthsMs = now + 90L * 24 * 60 * 60 * 1000
            val sixMonthsMs = now + 180L * 24 * 60 * 60 * 1000
            val twelveMonthsMs = now + 365L * 24 * 60 * 60 * 1000

            for (vehicleWithPolicies in allVehicles) {
                val current = vehicleWithPolicies.currentPolicy

                // Book Size
                if (!vehicleWithPolicies.vehicle.isClosed && current != null) {
                    val days = current.computeDaysRemaining(todayEpochDay)
                    if (days >= 0) {
                        activeBookVehicles++
                        activeBookPremium += current.totalPremium

                        // Pipeline
                        if (current.expiryDate in now..threeMonthsMs) {
                            pipe3Count++; pipe3Prem += current.totalPremium
                        }
                        if (current.expiryDate in now..sixMonthsMs) {
                            pipe6Count++; pipe6Prem += current.totalPremium
                        }
                        if (current.expiryDate in now..twelveMonthsMs) {
                            pipe12Count++; pipe12Prem += current.totalPremium
                        }
                    }
                }

                // Check missed current policies
                if (current != null && current.closeReason.isNullOrBlank() && current.renewedOn == null) {
                    val daysRemaining = current.computeDaysRemaining(todayEpochDay)
                    if (daysRemaining < 0) {
                        val daysOverdue = -daysRemaining
                        missedAll.add(vehicleWithPolicies)
                        premiumAtRisk += current.totalPremium

                        when {
                            daysOverdue <= 30 -> upTo30.add(vehicleWithPolicies)
                            daysOverdue <= 90 -> from31To90.add(vehicleWithPolicies)
                            else -> over90.add(vehicleWithPolicies)
                        }
                    }
                }

                // Check all policies under this vehicle for sales and performance in period
                for (p in vehicleWithPolicies.policies) {
                    // Sales
                    if (p.countsAsSale && p.saleDate in startMillis..endMillis) {
                        soldItems.add(vehicleWithPolicies)
                        salesTotalPremium += p.totalPremium
                        if (p.saleType == com.policydesk.app.domain.model.SaleType.NEW_BUSINESS) {
                            newBusinessCount++
                        } else {
                            renewalCount++
                        }
                        insurerSplit[p.insurer] = (insurerSplit[p.insurer] ?: 0.0) + p.totalPremium
                        sectorSplit[p.companySector] = (sectorSplit[p.companySector] ?: 0.0) + p.totalPremium
                    }

                    // Performance (due in this period)
                    if (p.expiryDate in startMillis..endMillis) {
                        dueInPeriod++
                        if (p.renewedOn != null) {
                            if (p.renewedOn <= p.expiryDate) {
                                onTimeInPeriod++
                            } else {
                                lateInPeriod++
                            }
                        } else {
                            missedInPeriod++
                        }
                    }
                }
            }

            val renewalRatePercent = if (dueInPeriod > 0) ((onTimeInPeriod + lateInPeriod).toDouble() / dueInPeriod) * 100 else 0.0
            val onTimeRatePercent = if (dueInPeriod > 0) (onTimeInPeriod.toDouble() / dueInPeriod) * 100 else 0.0

            // 5. 12-Month Sales Trend (last 12 months up to current month)
            val currentMonthDate = LocalDate.now()
            val trendBuckets = mutableListOf<com.policydesk.app.domain.model.MonthlyTrendItem>()
            for (i in 11 downTo 0) {
                val m = currentMonthDate.minusMonths(i.toLong())
                val start = LocalDate.of(m.year, m.monthValue, 1).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = LocalDate.of(m.year, m.monthValue, 1).plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
                val label = "${m.month.name.take(3)} '${(m.year % 100)}"

                var nbPrem = 0.0
                var renPrem = 0.0
                for (v in allVehicles) {
                    for (p in v.policies) {
                        if (p.countsAsSale && p.saleDate in start..end) {
                            if (p.saleType == com.policydesk.app.domain.model.SaleType.NEW_BUSINESS) {
                                nbPrem += p.totalPremium
                            } else {
                                renPrem += p.totalPremium
                            }
                        }
                    }
                }
                trendBuckets.add(
                    com.policydesk.app.domain.model.MonthlyTrendItem(
                        monthLabel = label,
                        newBusinessPremium = nbPrem,
                        renewalPremium = renPrem,
                        totalPremium = nbPrem + renPrem
                    )
                )
            }

            val salesSummary = com.policydesk.app.domain.model.SalesSummary(
                totalPremium = salesTotalPremium,
                totalCount = soldItems.size,
                newBusinessCount = newBusinessCount,
                renewalCount = renewalCount,
                splitByInsurer = insurerSplit,
                splitBySector = sectorSplit,
                soldPolicies = soldItems,
                monthlyTrends = trendBuckets
            )

            val missedSummary = com.policydesk.app.domain.model.MissedRenewalsSummary(
                totalCount = missedAll.size,
                premiumAtRisk = premiumAtRisk,
                upTo30Days = upTo30,
                from31To90Days = from31To90,
                over90Days = over90,
                allMissed = missedAll.sortedBy { it.currentPolicy?.expiryDate ?: Long.MAX_VALUE }
            )

            val performance = com.policydesk.app.domain.model.RenewalPerformance(
                policiesDue = dueInPeriod,
                renewedOnTime = onTimeInPeriod,
                renewedLate = lateInPeriod,
                missed = missedInPeriod,
                renewalRatePercent = renewalRatePercent,
                onTimeRatePercent = onTimeRatePercent
            )

            val analytics = com.policydesk.app.domain.model.AnalyticsSummary(
                premiumSold = salesTotalPremium,
                policiesSold = soldItems.size,
                renewalRatePercent = renewalRatePercent,
                onTimeRatePercent = onTimeRatePercent,
                premiumAtRisk = premiumAtRisk,
                renewalPipeline3Months = Pair(pipe3Count, pipe3Prem),
                renewalPipeline6Months = Pair(pipe6Count, pipe6Prem),
                renewalPipeline12Months = Pair(pipe12Count, pipe12Prem),
                bookSizeVehicles = activeBookVehicles,
                bookSizePremium = activeBookPremium,
                insurerMixShare = insurerSplit
            )

            com.policydesk.app.domain.model.ReportsAggregate(
                period = period,
                sales = salesSummary,
                missed = missedSummary,
                performance = performance,
                analytics = analytics
            )
        }
    }

    override suspend fun closeVehicle(regNo: String, reason: String) {
        val normalized = normalizeRegistration(regNo)
        vehicleDao.closeVehicle(normalized, reason)
        triggerSilentDriveSync()
    }

    override suspend fun softDeleteVehicle(regNo: String) {
        val normalized = normalizeRegistration(regNo)
        vehicleDao.softDeleteVehicle(normalized)
        triggerSilentDriveSync()
    }

    override suspend fun softDeleteVehicles(regNos: List<String>) {
        if (regNos.isNotEmpty()) {
            val normalizedList = regNos.map { normalizeRegistration(it) }
            vehicleDao.softDeleteVehicles(normalizedList)
            triggerSilentDriveSync()
        }
    }

    override suspend fun markPolicyRenewed(policyId: String, renewedOn: Long) {
        vehicleDao.markPolicyRenewed(policyId, renewedOn)
        triggerSilentDriveSync()
    }

    override suspend fun rollbackImport(vehicleRegNos: List<String>, policyIds: List<String>) {
        if (policyIds.isNotEmpty()) {
            vehicleDao.softDeletePolicies(policyIds)
        }
        if (vehicleRegNos.isNotEmpty()) {
            vehicleDao.softDeleteVehicles(vehicleRegNos)
        }
        triggerSilentDriveSync()
    }

    override fun getCustomerGroups(): Flow<List<com.policydesk.app.domain.model.CustomerGroup>> {
        return getAllVehicles().map { vehicles ->
            val today = LocalDate.now().toEpochDay()
            vehicles.groupBy { it.vehicle.customer.mobile }
                .map { (mobile, list) ->
                    val first = list.first().vehicle.customer
                    val totalPolicies = list.sumOf { it.policies.size }
                    val activePremium = list.sumOf { v ->
                        val p = v.currentPolicy
                        if (p != null && !v.vehicle.isClosed && p.computeDaysRemaining(today) >= 0) p.totalPremium else 0.0
                    }
                    com.policydesk.app.domain.model.CustomerGroup(
                        mobile = mobile,
                        name = first.name,
                        title = first.title,
                        address = first.address,
                        vehicles = list.sortedBy { it.vehicle.regNo },
                        totalPolicies = totalPolicies,
                        totalActivePremium = activePremium
                    )
                }
                .sortedByDescending { it.vehicles.size }
        }
    }

    override fun getCustomInsurers(sector: com.policydesk.app.domain.model.Sector): Flow<List<String>> {
        val standard = com.policydesk.app.domain.model.InsurerCatalog.getInsurersForSector(sector)
        return vehicleDao.getDistinctInsurersBySector(sector.name).map { list ->
            list.filter { it.isNotBlank() && it !in standard && !it.equals("Other", ignoreCase = true) }
        }
    }

    override suspend fun restoreVehiclesAndPolicies(vehiclesWithPolicies: List<VehicleWithPolicies>): Int {
        var count = 0
        for (item in vehiclesWithPolicies) {
            val normalized = normalizeRegistration(item.vehicle.regNo)
            val vehicle = item.vehicle.copy(regNo = normalized)
            vehicleDao.insertOrUpdateVehicle(VehicleEntity.fromDomain(vehicle))
            for (policy in item.policies) {
                val policyWithNorm = policy.copy(vehicleRegNo = normalized)
                vehicleDao.insertOrUpdatePolicy(PolicyEntity.fromDomain(policyWithNorm))
            }
            count++
        }
        triggerSilentDriveSync()
        return count
    }

    override suspend fun restoreSoftDeletedRecords(): Int {
        val count = vehicleDao.getSoftDeletedCount()
        if (count > 0) {
            vehicleDao.restoreAllSoftDeletedVehicles()
            vehicleDao.restoreAllSoftDeletedPolicies()
            triggerSilentDriveSync()
        }
        return count
    }
}

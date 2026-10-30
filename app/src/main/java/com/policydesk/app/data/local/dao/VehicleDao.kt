package com.policydesk.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.policydesk.app.data.local.entity.PolicyEntity
import com.policydesk.app.data.local.entity.VehicleEntity
import com.policydesk.app.data.local.entity.VehicleWithPoliciesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateVehicle(vehicle: VehicleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePolicy(policy: PolicyEntity)

    @Transaction
    suspend fun saveVehicleWithPolicy(vehicle: VehicleEntity, policy: PolicyEntity) {
        insertOrUpdateVehicle(vehicle)
        insertOrUpdatePolicy(policy)
    }

    @Query("SELECT COUNT(*) > 0 FROM vehicles WHERE regNo = :regNo AND deletedAt IS NULL")
    suspend fun exists(regNo: String): Boolean

    @Transaction
    @Query("SELECT * FROM vehicles WHERE regNo = :regNo AND deletedAt IS NULL")
    fun getVehicleWithPolicies(regNo: String): Flow<VehicleWithPoliciesEntity?>

    @Transaction
    @Query("SELECT * FROM vehicles WHERE regNo = :regNo AND deletedAt IS NULL")
    suspend fun getVehicleWithPoliciesDirect(regNo: String): VehicleWithPoliciesEntity?

    @Transaction
    @Query("SELECT * FROM vehicles WHERE deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getAllVehicles(): Flow<List<VehicleWithPoliciesEntity>>

    @Transaction
    @Query("""
        SELECT * FROM vehicles 
        WHERE deletedAt IS NULL 
        AND (
            regNo LIKE '%' || :query || '%' 
            OR name LIKE '%' || :query || '%' 
            OR mobile LIKE '%' || :query || '%'
            OR regNo IN (
                SELECT vehicleRegNo FROM policies 
                WHERE policyNumber LIKE '%' || :query || '%'
                AND deletedAt IS NULL
            )
        )
        ORDER BY updatedAt DESC
    """)
    fun searchVehicles(query: String): Flow<List<VehicleWithPoliciesEntity>>

    @Query("""
        SELECT DISTINCT makeModel FROM vehicles 
        WHERE makeModel IS NOT NULL AND makeModel != '' AND deletedAt IS NULL 
        ORDER BY makeModel ASC
    """)
    fun getDistinctMakeModels(): Flow<List<String>>

    @Query("""
        SELECT DISTINCT insurer FROM policies 
        WHERE companySector = :sector AND deletedAt IS NULL AND insurer != '' 
        ORDER BY insurer ASC
    """)
    fun getDistinctInsurersBySector(sector: String): Flow<List<String>>

    @Query("UPDATE vehicles SET deletedAt = :timestamp, updatedAt = :timestamp WHERE regNo = :regNo")
    suspend fun softDeleteVehicle(regNo: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE vehicles SET deletedAt = :timestamp, updatedAt = :timestamp WHERE regNo IN (:regNos)")
    suspend fun softDeleteVehicles(regNos: List<String>, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE policies SET deletedAt = :timestamp WHERE id IN (:policyIds)")
    suspend fun softDeletePolicies(policyIds: List<String>, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE policies SET renewedOn = :renewedOn, status = 'RENEWED', updatedAt = :timestamp WHERE id = :policyId")
    suspend fun markPolicyRenewed(policyId: String, renewedOn: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE vehicles SET isClosed = 1, closeReason = :reason, updatedAt = :timestamp WHERE regNo = :regNo")
    suspend fun closeVehicle(regNo: String, reason: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM vehicles WHERE deletedAt IS NOT NULL")
    suspend fun getSoftDeletedCount(): Int

    @Query("UPDATE vehicles SET deletedAt = NULL")
    suspend fun restoreAllSoftDeletedVehicles(): Int

    @Query("UPDATE policies SET deletedAt = NULL")
    suspend fun restoreAllSoftDeletedPolicies(): Int
}

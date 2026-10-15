package com.policydesk.app.data.drive

import android.content.Context
import android.net.Uri
import com.policydesk.app.domain.model.Customer
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicySource
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.SaleType
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.domain.model.Vehicle
import com.policydesk.app.domain.model.VehicleWithPolicies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object GoogleDriveManager {

    const val SNAPSHOT_FILE_NAME = "policydesk_snapshot.json"
    private const val PREFS_NAME = "policydesk_drive_prefs"
    private const val KEY_BACKUP_URI = "backup_file_uri"
    private const val KEY_LAST_SYNC_TIME = "last_sync_time"

    fun setSavedBackupUri(context: Context, uri: Uri) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BACKUP_URI, uri.toString())
            .apply()
    }

    fun getSavedBackupUri(context: Context): Uri? {
        val uriStr = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BACKUP_URI, null) ?: return null
        return Uri.parse(uriStr)
    }

    fun setLastSyncTime(context: Context, timestamp: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_SYNC_TIME, timestamp)
            .apply()
    }

    fun getLastSyncTime(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_SYNC_TIME, 0L)
    }

    suspend fun performSilentBackup(context: Context, snapshotJson: String): Result<Unit> {
        val uri = getSavedBackupUri(context) ?: return Result.failure(IllegalStateException("No Google Drive backup destination configured."))
        return writeSnapshotToUri(context, uri, snapshotJson).onSuccess {
            setLastSyncTime(context, System.currentTimeMillis())
        }
    }

    suspend fun writeSnapshotToUri(context: Context, uri: Uri, snapshotJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { it.write(snapshotJson) }
            } ?: throw IllegalStateException("Unable to open output stream to Google Drive storage.")
        }
    }

    suspend fun readSnapshotFromUri(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { it.readText() }
            } ?: throw IllegalStateException("Unable to open input stream from Google Drive storage.")
        }
    }

    fun createSnapshotJson(vehicles: List<VehicleWithPolicies>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("app", "PolicyDesk")

        val vehiclesArray = JSONArray()
        for (v in vehicles) {
            val vObj = JSONObject()
            vObj.put("regNo", v.vehicle.regNo)
            vObj.put("makeModel", v.vehicle.makeModel ?: "")
            vObj.put("vin", v.vehicle.vin ?: "")
            vObj.put("engineNo", v.vehicle.engineNo ?: "")
            vObj.put("notes", v.vehicle.notes ?: "")
            vObj.put("createdAt", v.vehicle.createdAt)
            vObj.put("updatedAt", v.vehicle.updatedAt)
            vObj.put("isClosed", v.vehicle.isClosed)
            vObj.put("closeReason", v.vehicle.closeReason ?: "")

            val cObj = JSONObject()
            cObj.put("title", v.vehicle.customer.title ?: "")
            cObj.put("name", v.vehicle.customer.name)
            cObj.put("mobile", v.vehicle.customer.mobile)
            cObj.put("email", v.vehicle.customer.email ?: "")
            cObj.put("address", v.vehicle.customer.address)
            vObj.put("customer", cObj)

            val pArray = JSONArray()
            for (p in v.policies) {
                val pObj = JSONObject()
                pObj.put("id", p.id)
                pObj.put("vehicleRegNo", p.vehicleRegNo)
                pObj.put("companySector", p.companySector.name)
                pObj.put("insurer", p.insurer)
                pObj.put("policyNumber", p.policyNumber)
                pObj.put("expiryDate", p.expiryDate)
                pObj.put("startDate", p.startDate ?: 0L)
                pObj.put("totalPremium", p.totalPremium)
                pObj.put("status", p.status.name)
                pObj.put("source", p.source.name)
                pObj.put("attachmentId", p.attachmentId ?: "")
                pObj.put("renewedOn", p.renewedOn ?: 0L)
                pObj.put("closeReason", p.closeReason ?: "")
                pObj.put("saleType", p.saleType.name)
                pObj.put("saleDate", p.saleDate)
                pObj.put("countsAsSale", p.countsAsSale)
                pArray.put(pObj)
            }
            vObj.put("policies", pArray)
            vehiclesArray.put(vObj)
        }
        root.put("vehicles", vehiclesArray)
        return root.toString(2)
    }

    fun parseSnapshotJson(jsonStr: String): List<VehicleWithPolicies> {
        val root = JSONObject(jsonStr)
        val vehiclesArray = root.getJSONArray("vehicles")
        val list = mutableListOf<VehicleWithPolicies>()

        for (i in 0 until vehiclesArray.length()) {
            val vObj = vehiclesArray.getJSONObject(i)
            val cObj = vObj.getJSONObject("customer")
            val customer = Customer(
                title = cObj.optString("title").ifBlank { null },
                name = cObj.getString("name"),
                mobile = cObj.getString("mobile"),
                email = cObj.optString("email").ifBlank { null },
                address = cObj.getString("address")
            )
            val vehicle = Vehicle(
                regNo = vObj.getString("regNo"),
                customer = customer,
                makeModel = vObj.optString("makeModel").ifBlank { null },
                vin = vObj.optString("vin").ifBlank { null },
                engineNo = vObj.optString("engineNo").ifBlank { null },
                notes = vObj.optString("notes").ifBlank { null },
                createdAt = vObj.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = vObj.optLong("updatedAt", System.currentTimeMillis()),
                isClosed = vObj.optBoolean("isClosed", false),
                closeReason = vObj.optString("closeReason").ifBlank { null }
            )

            val pArray = vObj.getJSONArray("policies")
            val policies = mutableListOf<Policy>()
            for (j in 0 until pArray.length()) {
                val pObj = pArray.getJSONObject(j)
                val policy = Policy(
                    id = pObj.getString("id"),
                    vehicleRegNo = pObj.getString("vehicleRegNo"),
                    companySector = Sector.valueOf(pObj.getString("companySector")),
                    insurer = pObj.getString("insurer"),
                    policyNumber = pObj.getString("policyNumber"),
                    expiryDate = pObj.getLong("expiryDate"),
                    startDate = pObj.optLong("startDate").takeIf { it > 0 },
                    totalPremium = pObj.getDouble("totalPremium"),
                    status = PolicyStatus.valueOf(pObj.getString("status")),
                    source = PolicySource.valueOf(pObj.getString("source")),
                    attachmentId = pObj.optString("attachmentId").ifBlank { null },
                    renewedOn = pObj.optLong("renewedOn").takeIf { it > 0 },
                    closeReason = pObj.optString("closeReason").ifBlank { null },
                    saleType = SaleType.valueOf(pObj.getString("saleType")),
                    saleDate = pObj.optLong("saleDate", System.currentTimeMillis()),
                    countsAsSale = pObj.optBoolean("countsAsSale", true)
                )
                policies.add(policy)
            }
            list.add(VehicleWithPolicies(vehicle, policies))
        }
        return list
    }
}

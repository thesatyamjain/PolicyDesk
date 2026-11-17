package com.policydesk.app.data.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PolicyDocumentManager {

    private const val POLICIES_DIR = "policies"

    private fun getPoliciesDirectory(context: Context): File {
        val dir = File(context.filesDir, POLICIES_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Copies a source PDF (from content:// or file:// URI) into the app's private internal storage.
     * Returns the relative filename (e.g. "<policyId>.pdf") to be stored in Policy.attachmentId.
     */
    fun savePolicyDocument(context: Context, sourceUri: Uri, policyId: String): String? {
        return try {
            val dir = getPoliciesDirectory(context)
            val targetFile = File(dir, "$policyId.pdf")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            if (targetFile.exists() && targetFile.length() > 0) {
                "$policyId.pdf"
            } else {
                targetFile.delete()
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Checks if a document exists locally and returns its File handle.
     */
    fun getPolicyDocumentFile(context: Context, attachmentId: String): File? {
        if (attachmentId.isBlank()) return null
        val cleanName = attachmentId.removePrefix("policies/").substringAfterLast('/')
        val file = File(getPoliciesDirectory(context), cleanName)
        return if (file.exists() && file.length() > 0) file else null
    }

    /**
     * Checks if a document is stored locally on the device.
     */
    fun hasLocalDocument(context: Context, attachmentId: String?): Boolean {
        if (attachmentId.isNullOrBlank()) return false
        return getPolicyDocumentFile(context, attachmentId) != null
    }

    /**
     * Returns the file size in formatted readable string (e.g. "450 KB").
     */
    fun getDocumentSizeFormatted(context: Context, attachmentId: String): String? {
        val file = getPolicyDocumentFile(context, attachmentId) ?: return null
        val bytes = file.length()
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format(java.util.Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
        }
    }

    /**
     * Opens the stored policy PDF using the system or preferred PDF viewer application.
     */
    fun openPolicyDocument(context: Context, attachmentId: String): Boolean {
        val file = getPolicyDocumentFile(context, attachmentId)
        if (file == null) {
            Toast.makeText(context, "Policy document file not found", Toast.LENGTH_SHORT).show()
            return false
        }

        return try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            Toast.makeText(context, "No PDF viewer found. Please install a PDF reader app.", Toast.LENGTH_LONG).show()
            false
        }
    }

    /**
     * Shares the policy PDF via Android system share sheet (e.g. WhatsApp, Email, Drive).
     */
    fun sharePolicyDocument(
        context: Context,
        attachmentId: String,
        regNo: String,
        customerName: String? = null,
        insurer: String? = null
    ): Boolean {
        val file = getPolicyDocumentFile(context, attachmentId)
        if (file == null) {
            Toast.makeText(context, "Policy document file not found", Toast.LENGTH_SHORT).show()
            return false
        }

        return try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val clientGreeting = if (!customerName.isNullOrBlank()) "Dear $customerName," else "Hello,"
            val insurerText = if (!insurer.isNullOrBlank()) " with $insurer" else ""
            val messageText = "$clientGreeting\n\nPlease find attached the insurance policy schedule$insurerText for vehicle $regNo.\n\nShared via PolicyDesk."

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Insurance Policy - $regNo")
                putExtra(Intent.EXTRA_TEXT, messageText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Policy PDF ($regNo)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share document: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Deletes the stored document file from private storage.
     */
    fun deletePolicyDocument(context: Context, attachmentId: String): Boolean {
        return try {
            val file = getPolicyDocumentFile(context, attachmentId)
            file?.delete() ?: false
        } catch (_: Exception) {
            false
        }
    }
}

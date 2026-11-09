package com.policydesk.app.data.profile

import android.content.Context
import android.content.SharedPreferences
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.RegistrationNormalizer

data class AdvisorProfile(
    val advisorName: String = "",
    val agencyName: String = "",
    val advisorPhone: String = "",
    val advisorEmail: String = "",
    val advisorPhotoUrl: String = "",
    val customWhatsappTemplate: String = ""
) {
    val signature: String
        get() = when {
            advisorName.isNotBlank() && agencyName.isNotBlank() -> "$advisorName • $agencyName"
            advisorName.isNotBlank() -> advisorName
            agencyName.isNotBlank() -> agencyName
            else -> "PolicyDesk Advisory"
        }
}

object AdvisorProfileManager {
    private const val PREF_NAME = "policydesk_advisor_profile"
    private const val KEY_NAME = "advisor_name"
    private const val KEY_AGENCY = "agency_name"
    private const val KEY_PHONE = "advisor_phone"
    private const val KEY_EMAIL = "advisor_email"
    private const val KEY_PHOTO_URL = "advisor_photo_url"
    private const val KEY_TEMPLATE = "custom_whatsapp_template"

    const val DEFAULT_TEMPLATE =
        "Hello {customer}, your vehicle policy for {vehicle} ({insurer}) is due for renewal on {expiry}. Kindly review and let us know to initiate renewal without inspection lapse.\n\nRegards,\n{signature}"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getProfile(context: Context): AdvisorProfile {
        val prefs = getPrefs(context)
        return AdvisorProfile(
            advisorName = prefs.getString(KEY_NAME, "") ?: "",
            agencyName = prefs.getString(KEY_AGENCY, "") ?: "",
            advisorPhone = prefs.getString(KEY_PHONE, "") ?: "",
            advisorEmail = prefs.getString(KEY_EMAIL, "") ?: "",
            advisorPhotoUrl = prefs.getString(KEY_PHOTO_URL, "") ?: "",
            customWhatsappTemplate = prefs.getString(KEY_TEMPLATE, "") ?: ""
        )
    }

    fun saveProfile(context: Context, profile: AdvisorProfile) {
        getPrefs(context).edit()
            .putString(KEY_NAME, profile.advisorName)
            .putString(KEY_AGENCY, profile.agencyName)
            .putString(KEY_PHONE, profile.advisorPhone)
            .putString(KEY_EMAIL, profile.advisorEmail)
            .putString(KEY_PHOTO_URL, profile.advisorPhotoUrl)
            .putString(KEY_TEMPLATE, profile.customWhatsappTemplate)
            .apply()
    }

    /**
     * Auto-populates empty advisor profile fields from connected Google Account.
     * Automatically applies Title Case to Google display name.
     * Preserves existing custom advisor name or agency if already configured.
     */
    fun syncFromGoogleAccount(
        context: Context,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ): AdvisorProfile {
        val current = getProfile(context)
        val candidateName = displayName?.takeIf { it.isNotBlank() } ?: email?.substringBefore("@")
        val formattedName = candidateName?.let { FieldValidators.formatNameTitleCase(it) } ?: ""

        val updated = current.copy(
            advisorName = if (current.advisorName.isBlank() && formattedName.isNotBlank()) formattedName else current.advisorName,
            advisorEmail = if (current.advisorEmail.isBlank() && !email.isNullOrBlank()) email else current.advisorEmail,
            advisorPhotoUrl = if (current.advisorPhotoUrl.isBlank() && !photoUrl.isNullOrBlank()) photoUrl else current.advisorPhotoUrl
        )
        saveProfile(context, updated)
        return updated
    }

    fun formatRenewalMessage(
        context: Context,
        customerName: String,
        regNo: String,
        insurer: String,
        expiryDate: String,
        premium: String = ""
    ): String {
        val profile = getProfile(context)
        val template = if (profile.customWhatsappTemplate.isNotBlank()) {
            profile.customWhatsappTemplate
        } else {
            DEFAULT_TEMPLATE
        }
        val cleanCustomer = customerName.ifBlank { "Customer" }
        val cleanVehicle = RegistrationNormalizer.formatForDisplay(regNo)
        val cleanInsurer = insurer.ifBlank { "Insurance" }
        val cleanExpiry = expiryDate.ifBlank { "soon" }
        val signature = profile.signature

        return template
            .replace("{customer}", cleanCustomer)
            .replace("{vehicle}", cleanVehicle)
            .replace("{insurer}", cleanInsurer)
            .replace("{expiry}", cleanExpiry)
            .replace("{premium}", premium)
            .replace("{signature}", signature)
    }
}

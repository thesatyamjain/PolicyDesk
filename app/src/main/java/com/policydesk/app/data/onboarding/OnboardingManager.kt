package com.policydesk.app.data.onboarding

import android.content.Context
import android.content.SharedPreferences

object OnboardingManager {
    private const val PREFS_NAME = "policydesk_onboarding_prefs"
    private const val KEY_COMPLETED = "is_onboarding_completed"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isOnboardingCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_COMPLETED, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_COMPLETED, completed).apply()
    }

    fun resetOnboarding(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_COMPLETED, false).apply()
    }
}

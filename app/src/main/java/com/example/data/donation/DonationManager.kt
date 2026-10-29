package com.example.data.donation

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DonationManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isSupporter = MutableStateFlow(prefs.getBoolean(KEY_IS_SUPPORTER, false))
    val isSupporter: StateFlow<Boolean> = _isSupporter.asStateFlow()

    init {
        // Record initial install timestamp if not set
        if (prefs.getLong(KEY_INSTALL_TIMESTAMP, 0L) == 0L) {
            prefs.edit().putLong(KEY_INSTALL_TIMESTAMP, System.currentTimeMillis()).apply()
        }
        // Increment app launch count
        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 0) + 1
        prefs.edit().putInt(KEY_LAUNCH_COUNT, launches).apply()
    }

    fun recordSuccessfulOperation() {
        val currentCount = prefs.getInt(KEY_OPERATION_COUNT, 0) + 1
        prefs.edit().putInt(KEY_OPERATION_COUNT, currentCount).apply()
    }

    fun getSuccessfulOperationCount(): Int {
        return prefs.getInt(KEY_OPERATION_COUNT, 0)
    }

    fun shouldShowBanner(): Boolean {
        if (_isSupporter.value) return false

        val installTime = prefs.getLong(KEY_INSTALL_TIMESTAMP, System.currentTimeMillis())
        val daysSinceInstall = (System.currentTimeMillis() - installTime) / (1000 * 60 * 60 * 24)
        val operationCount = prefs.getInt(KEY_OPERATION_COUNT, 0)
        val launchCount = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        val lastDismissTime = prefs.getLong(KEY_BANNER_DISMISSED_TIMESTAMP, 0L)
        val daysSinceDismiss = (System.currentTimeMillis() - lastDismissTime) / (1000 * 60 * 60 * 24)

        return operationCount >= 3 && launchCount >= 3 && daysSinceInstall >= 2 && daysSinceDismiss >= 14
    }

    fun dismissBanner() {
        prefs.edit().putLong(KEY_BANNER_DISMISSED_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    fun shouldShowAutoPrompt(): Boolean {
        if (_isSupporter.value) return false

        val operationCount = prefs.getInt(KEY_OPERATION_COUNT, 0)
        val lastPromptTime = prefs.getLong(KEY_AUTO_PROMPT_TIMESTAMP, 0L)
        val daysSinceLastPrompt = (System.currentTimeMillis() - lastPromptTime) / (1000 * 60 * 60 * 24)

        return operationCount >= 7 && (lastPromptTime == 0L || daysSinceLastPrompt >= 30)
    }

    fun recordAutoPromptShown() {
        prefs.edit().putLong(KEY_AUTO_PROMPT_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    fun markAsSupporter() {
        prefs.edit().putBoolean(KEY_IS_SUPPORTER, true).apply()
        _isSupporter.value = true
    }

    companion object {
        private const val PREFS_NAME = "daily_curator_donation_prefs"
        private const val KEY_IS_SUPPORTER = "is_supporter"
        private const val KEY_INSTALL_TIMESTAMP = "install_timestamp"
        private const val KEY_OPERATION_COUNT = "operation_count"
        private const val KEY_LAUNCH_COUNT = "launch_count"
        private const val KEY_BANNER_DISMISSED_TIMESTAMP = "banner_dismissed_timestamp"
        private const val KEY_AUTO_PROMPT_TIMESTAMP = "auto_prompt_timestamp"

        @Volatile
        private var INSTANCE: DonationManager? = null

        fun getInstance(context: Context): DonationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DonationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

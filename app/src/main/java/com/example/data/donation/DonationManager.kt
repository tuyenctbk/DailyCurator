package com.example.data.donation

import android.content.Context
import com.example.data.engagement.EngagementManager
import kotlinx.coroutines.flow.StateFlow

/**
 * Backwards-compatible facade delegating directly to the unified EngagementManager.
 * Ensures single source of truth for engagement, anti-fatigue cooldowns, and patron status.
 */
class DonationManager private constructor(context: Context) {

    private val engagementManager = EngagementManager.getInstance(context)

    val isSupporter: StateFlow<Boolean> = engagementManager.isSupporter

    fun recordSuccessfulOperation() {
        engagementManager.recordSuccessfulOperation()
    }

    fun getSuccessfulOperationCount(): Int {
        return engagementManager.getSuccessfulOperationCount()
    }

    fun shouldShowBanner(): Boolean {
        return engagementManager.shouldShowBanner()
    }

    fun dismissBanner() {
        engagementManager.dismissBanner()
    }

    fun shouldShowAutoPrompt(): Boolean {
        return engagementManager.shouldShowDonationModal()
    }

    fun recordAutoPromptShown() {
        engagementManager.recordDonationModalShown()
    }

    fun markAsSupporter() {
        engagementManager.markAsSupporter()
    }

    companion object {
        @Volatile
        private var INSTANCE: DonationManager? = null

        fun getInstance(context: Context): DonationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DonationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

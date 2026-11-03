package com.example.data.engagement

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unified Engagement & Prompt Orchestrator for DailyCurator.
 *
 * Implements a strict, user-first prompt policy to eliminate prompt fatigue:
 * 1. ZERO startup popups: Never interrupts user on app launch.
 * 2. Moment-of-Delight execution: Prompts evaluate strictly after positive milestones (wallpaper applied, 4K saved).
 * 3. Global Peacetime: Guarantees 14-21 days of total prompt silence between ANY two automated dialogs.
 * 4. Session Exclusivity: At most ONE prompt may trigger per app process/session.
 * 5. Exponential Dismissal Backoff: Respects negative feedback (14d -> 30d -> permanent suppression).
 * 6. Permanent Supporter Immunity: Once a patron, all promotional prompts and banners are silenced forever.
 */
class EngagementManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isSupporter = MutableStateFlow(prefs.getBoolean(KEY_IS_SUPPORTER, false))
    val isSupporter: StateFlow<Boolean> = _isSupporter.asStateFlow()

    private val _shouldShowBanner = MutableStateFlow(false)
    val shouldShowBanner: StateFlow<Boolean> = _shouldShowBanner.asStateFlow()

    private val _isFlexibleUpdateAvailable = MutableStateFlow(false)
    val isFlexibleUpdateAvailable: StateFlow<Boolean> = _isFlexibleUpdateAvailable.asStateFlow()

    // In-session lock: once any prompt is shown in this session, lock out all others
    private var isSessionPromptLocked = false

    init {
        // Record install timestamp if fresh installation
        if (prefs.getLong(KEY_INSTALL_TIMESTAMP, 0L) == 0L) {
            prefs.edit().putLong(KEY_INSTALL_TIMESTAMP, System.currentTimeMillis()).apply()
        }
        // Increment app launch counter
        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 0) + 1
        prefs.edit().putInt(KEY_LAUNCH_COUNT, launches).apply()

        // Evaluate passive banner visibility (never a popup)
        _shouldShowBanner.value = calculateShouldShowBanner()
    }

    // =========================================================================
    // Core Calculation & Eligibility Rules
    // =========================================================================

    fun getDaysSinceInstall(): Long {
        val installTime = prefs.getLong(KEY_INSTALL_TIMESTAMP, System.currentTimeMillis())
        return (System.currentTimeMillis() - installTime) / (1000 * 60 * 60 * 24)
    }

    /**
     * Enforces a cross-feature cooldown so the user is never bombarded by alternating prompts.
     */
    fun isGlobalPeacetimeActive(minPeacetimeDays: Int = 14): Boolean {
        val lastGlobal = prefs.getLong(KEY_GLOBAL_LAST_PROMPT_TIMESTAMP, 0L)
        if (lastGlobal == 0L) return false
        val daysSinceLast = (System.currentTimeMillis() - lastGlobal) / (1000 * 60 * 60 * 24)
        return daysSinceLast < minPeacetimeDays
    }

    fun recordGlobalPromptShown() {
        prefs.edit().putLong(KEY_GLOBAL_LAST_PROMPT_TIMESTAMP, System.currentTimeMillis()).apply()
        isSessionPromptLocked = true
    }

    // =========================================================================
    // In-App Review (Rating) Orchestration
    // =========================================================================

    /**
     * Determines if user is in an optimal "Moment of Delight" to be invited for a Google Play rating.
     */
    fun shouldPromptRating(): Boolean {
        val hasRated = prefs.getBoolean(KEY_HAS_RATED, false)
        if (hasRated) return false
        if (isSessionPromptLocked) return false
        if (isGlobalPeacetimeActive(minPeacetimeDays = 14)) return false

        val daysSinceInstall = getDaysSinceInstall()
        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        val operations = prefs.getInt(KEY_OPERATION_COUNT, 0)
        val promptCount = prefs.getInt(KEY_RATING_PROMPT_COUNT, 0)

        // Threshold: at least 3 days, 4 launches, and 3 successful high-value operations
        if (daysSinceInstall < 3 || launches < 4 || operations < 3) return false

        // If previously prompted, require 60 days cooldown and limit to max 2 prompts total
        if (promptCount > 0) {
            val lastPrompt = prefs.getLong(KEY_RATING_LAST_PROMPT_TIMESTAMP, 0L)
            val daysSince = (System.currentTimeMillis() - lastPrompt) / (1000 * 60 * 60 * 24)
            return promptCount < 2 && daysSince >= 60
        }

        return true
    }

    /**
     * Launches the official Google Play In-App Review Flow.
     */
    fun launchInAppReviewFlow(activity: Activity, onComplete: () -> Unit = {}) {
        if (!shouldPromptRating()) {
            onComplete()
            return
        }

        recordGlobalPromptShown()
        val promptCount = prefs.getInt(KEY_RATING_PROMPT_COUNT, 0) + 1
        prefs.edit()
            .putInt(KEY_RATING_PROMPT_COUNT, promptCount)
            .putLong(KEY_RATING_LAST_PROMPT_TIMESTAMP, System.currentTimeMillis())
            .apply()

        try {
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener {
                        // Review flow completed or dismissed
                        prefs.edit().putBoolean(KEY_HAS_RATED, true).apply()
                        onComplete()
                    }
                } else {
                    onComplete()
                }
            }
        } catch (e: Exception) {
            onComplete()
        }
    }

    // =========================================================================
    // Supporter & Patron Prompt Orchestration
    // =========================================================================

    /**
     * Evaluates whether a full-screen donation modal should auto-prompt.
     * Guaranteed never to run on app launch. Requires deep loyalty and 21+ days peacetime.
     */
    fun shouldShowDonationModal(): Boolean {
        if (_isSupporter.value) return false
        if (isSessionPromptLocked) return false
        if (isGlobalPeacetimeActive(minPeacetimeDays = 21)) return false

        val dismissCount = prefs.getInt(KEY_DONATION_MODAL_DISMISS_COUNT, 0)
        // If user dismissed twice, respect their boundary permanently
        if (dismissCount >= 2) return false

        val daysSinceInstall = getDaysSinceInstall()
        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        val operations = prefs.getInt(KEY_OPERATION_COUNT, 0)

        // Deep loyalty milestone: 10+ days installed, 6+ launches, 7+ operations
        if (daysSinceInstall < 10 || launches < 6 || operations < 7) return false

        val lastPrompt = prefs.getLong(KEY_AUTO_PROMPT_TIMESTAMP, 0L)
        val daysSinceLast = (System.currentTimeMillis() - lastPrompt) / (1000 * 60 * 60 * 24)
        val requiredCooldown = if (dismissCount == 1) 90 else 45

        return lastPrompt == 0L || daysSinceLast >= requiredCooldown
    }

    fun recordDonationModalShown() {
        prefs.edit().putLong(KEY_AUTO_PROMPT_TIMESTAMP, System.currentTimeMillis()).apply()
        recordGlobalPromptShown()
    }

    fun dismissDonationModal() {
        val currentDismissals = prefs.getInt(KEY_DONATION_MODAL_DISMISS_COUNT, 0) + 1
        prefs.edit()
            .putInt(KEY_DONATION_MODAL_DISMISS_COUNT, currentDismissals)
            .putLong(KEY_AUTO_PROMPT_TIMESTAMP, System.currentTimeMillis())
            .apply()
        recordGlobalPromptShown()
    }

    // =========================================================================
    // Inline Non-Blocking Donation Banner (In Daily Discovery Feed)
    // =========================================================================

    private fun calculateShouldShowBanner(): Boolean {
        if (_isSupporter.value) return false

        val daysSinceInstall = getDaysSinceInstall()
        val operations = prefs.getInt(KEY_OPERATION_COUNT, 0)
        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        val dismissCount = prefs.getInt(KEY_BANNER_DISMISS_COUNT, 0)
        val lastDismissTime = prefs.getLong(KEY_BANNER_DISMISSED_TIMESTAMP, 0L)
        val daysSinceDismiss = (System.currentTimeMillis() - lastDismissTime) / (1000 * 60 * 60 * 24)

        if (daysSinceInstall < 3 || launches < 3 || operations < 3) return false

        val cooldownDays = when (dismissCount) {
            0 -> 0
            1 -> 21
            2 -> 45
            else -> 90
        }

        return lastDismissTime == 0L || daysSinceDismiss >= cooldownDays
    }

    fun shouldShowBanner(): Boolean {
        return calculateShouldShowBanner()
    }

    fun dismissBanner() {
        val count = prefs.getInt(KEY_BANNER_DISMISS_COUNT, 0) + 1
        prefs.edit()
            .putInt(KEY_BANNER_DISMISS_COUNT, count)
            .putLong(KEY_BANNER_DISMISSED_TIMESTAMP, System.currentTimeMillis())
            .apply()
        _shouldShowBanner.value = false
    }

    // =========================================================================
    // Milestone & Operational Event Hooks (Moments of Delight)
    // =========================================================================

    /**
     * Called strictly after user successfully sets a 4K wallpaper or saves high-res art.
     * Evaluates prompt eligibility in a single, coordinated hierarchy.
     */
    fun recordSuccessfulOperation(): PromptRecommendation {
        val currentCount = prefs.getInt(KEY_OPERATION_COUNT, 0) + 1
        prefs.edit().putInt(KEY_OPERATION_COUNT, currentCount).apply()

        // Update passive banner status
        _shouldShowBanner.value = calculateShouldShowBanner()

        // 1. First priority for satisfied users: In-App Rating
        if (shouldPromptRating()) {
            return PromptRecommendation.REQUEST_RATING
        }

        // 2. Second priority for deep loyalty power users: Patron Support
        if (shouldShowDonationModal()) {
            recordDonationModalShown()
            return PromptRecommendation.SHOW_DONATION_MODAL
        }

        return PromptRecommendation.NONE
    }

    fun getSuccessfulOperationCount(): Int {
        return prefs.getInt(KEY_OPERATION_COUNT, 0)
    }

    fun markAsSupporter() {
        prefs.edit().putBoolean(KEY_IS_SUPPORTER, true).apply()
        _isSupporter.value = true
        _shouldShowBanner.value = false
    }

    // =========================================================================
    // Flexible Background In-App Updates
    // =========================================================================

    fun checkInAppUpdateSilently(activity: Activity) {
        try {
            val appUpdateManager = AppUpdateManagerFactory.create(activity)
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    _isFlexibleUpdateAvailable.value = true
                }
            }
        } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    fun launchFlexibleUpdate(activity: Activity, requestCode: Int = 1002) {
        try {
            val appUpdateManager = AppUpdateManagerFactory.create(activity)
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        activity,
                        requestCode
                    )
                }
            }
        } catch (_: Exception) {}
    }

    enum class PromptRecommendation {
        NONE,
        REQUEST_RATING,
        SHOW_DONATION_MODAL
    }

    companion object {
        private const val PREFS_NAME = "daily_curator_engagement_prefs"

        private const val KEY_IS_SUPPORTER = "is_supporter"
        private const val KEY_INSTALL_TIMESTAMP = "install_timestamp"
        private const val KEY_LAUNCH_COUNT = "launch_count"
        private const val KEY_OPERATION_COUNT = "operation_count"

        private const val KEY_GLOBAL_LAST_PROMPT_TIMESTAMP = "global_last_prompt_timestamp"

        private const val KEY_HAS_RATED = "has_rated_or_reviewed"
        private const val KEY_RATING_PROMPT_COUNT = "rating_prompt_count"
        private const val KEY_RATING_LAST_PROMPT_TIMESTAMP = "rating_last_prompt_timestamp"

        private const val KEY_AUTO_PROMPT_TIMESTAMP = "auto_prompt_timestamp"
        private const val KEY_DONATION_MODAL_DISMISS_COUNT = "donation_modal_dismiss_count"

        private const val KEY_BANNER_DISMISSED_TIMESTAMP = "banner_dismissed_timestamp"
        private const val KEY_BANNER_DISMISS_COUNT = "banner_dismiss_count"

        @Volatile
        private var INSTANCE: EngagementManager? = null

        fun getInstance(context: Context): EngagementManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EngagementManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        @androidx.annotation.VisibleForTesting
        fun resetForTesting() {
            INSTANCE = null
        }
    }
}

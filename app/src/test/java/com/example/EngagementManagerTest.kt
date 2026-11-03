package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.engagement.EngagementManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EngagementManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        EngagementManager.resetForTesting()
        val prefs = context.getSharedPreferences("daily_curator_engagement_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @org.junit.After
    fun tearDown() {
        EngagementManager.resetForTesting()
        val prefs = context.getSharedPreferences("daily_curator_engagement_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun `test fresh install starts with zero prompt interruption`() {
        val manager = EngagementManager.getInstance(context)

        // Fresh install: 0 days, 1 launch, 0 operations
        assertFalse("Rating must not trigger on fresh install", manager.shouldPromptRating())
        assertFalse("Donation modal must not trigger on fresh install", manager.shouldShowDonationModal())
        assertFalse("Banner must not show on fresh install", manager.shouldShowBanner())
    }

    @Test
    fun `test rating prompt triggers strictly after maturity threshold`() {
        val prefs = context.getSharedPreferences("daily_curator_engagement_prefs", Context.MODE_PRIVATE)
        val fourDaysAgo = System.currentTimeMillis() - (4L * 24 * 60 * 60 * 1000)

        // Seed mature user state
        prefs.edit()
            .putLong("install_timestamp", fourDaysAgo)
            .putInt("launch_count", 5)
            .putInt("operation_count", 3)
            .commit()

        val manager = EngagementManager.getInstance(context)
        assertTrue("Rating should qualify after 4 days, 5 launches, and 3 operations", manager.shouldPromptRating())

        // Recommendation on successful operation should be REQUEST_RATING
        val recommendation = manager.recordSuccessfulOperation()
        assertEquals(EngagementManager.PromptRecommendation.REQUEST_RATING, recommendation)
    }

    @Test
    fun `test global peacetime and session lock suppress subsequent prompts`() {
        val prefs = context.getSharedPreferences("daily_curator_engagement_prefs", Context.MODE_PRIVATE)
        val fiveDaysAgo = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000)

        prefs.edit()
            .putLong("install_timestamp", fiveDaysAgo)
            .putInt("launch_count", 6)
            .putInt("operation_count", 4)
            .commit()

        val manager = EngagementManager.getInstance(context)
        assertTrue(manager.shouldPromptRating())

        // Record that a prompt was shown
        manager.recordGlobalPromptShown()

        // Session lock and peacetime must immediately block further prompts
        assertFalse("Rating must be blocked after session lock", manager.shouldPromptRating())
        assertFalse("Donation modal must be blocked after session lock", manager.shouldShowDonationModal())
        assertTrue("Global peacetime must be active", manager.isGlobalPeacetimeActive(minPeacetimeDays = 14))
    }

    @Test
    fun `test donation modal requires deep loyalty and respects dismissal backoff`() {
        val prefs = context.getSharedPreferences("daily_curator_engagement_prefs", Context.MODE_PRIVATE)
        val manager = EngagementManager.getInstance(context)

        // Less than 10 days installed -> false
        val fiveDaysAgo = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000)
        prefs.edit()
            .putLong("install_timestamp", fiveDaysAgo)
            .putInt("launch_count", 8)
            .putInt("operation_count", 10)
            .commit()
        assertFalse("Must not show before 10 days install", manager.shouldShowDonationModal())

        // 12 days installed, 7 launches, 8 operations -> eligible
        val twelveDaysAgo = System.currentTimeMillis() - (12L * 24 * 60 * 60 * 1000)
        prefs.edit()
            .putLong("install_timestamp", twelveDaysAgo)
            .putInt("launch_count", 7)
            .putInt("operation_count", 8)
            .commit()
        assertTrue("Should be eligible after deep loyalty threshold", manager.shouldShowDonationModal())

        // User dismisses once -> enters 45-day cooldown
        manager.dismissDonationModal()
        assertFalse("Must be in cooldown after first dismissal", manager.shouldShowDonationModal())

        // User dismisses twice -> permanently suppressed
        manager.dismissDonationModal()
        assertFalse("Must be permanently suppressed after 2 dismissals", manager.shouldShowDonationModal())
    }

    @Test
    fun `test patron supporter enjoys permanent immunity from all prompts and banners`() {
        val prefs = context.getSharedPreferences("daily_curator_engagement_prefs", Context.MODE_PRIVATE)
        val manager = EngagementManager.getInstance(context)

        // Seed loyal usage
        val monthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        prefs.edit()
            .putLong("install_timestamp", monthAgo)
            .putInt("launch_count", 20)
            .putInt("operation_count", 15)
            .commit()

        manager.markAsSupporter()

        assertTrue("User must be recognized as supporter", manager.isSupporter.value)
        assertFalse("Supporter must never see donation modal", manager.shouldShowDonationModal())
        assertFalse("Supporter must never see patron banner", manager.shouldShowBanner())
    }
}


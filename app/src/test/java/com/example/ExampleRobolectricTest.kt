package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DailyCuratorDatabase
import com.example.data.repository.CuratedMasterpieces
import com.example.data.repository.CuratorRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DailyCurator", appName)
    }

    @Test
    fun `curated masterpieces contains valid public domain entries`() {
        val masterpieces = CuratedMasterpieces.allMasterpieces
        assertTrue(masterpieces.isNotEmpty())

        val dailyPick = CuratedMasterpieces.getDailyCuratorPick()
        assertNotNull(dailyPick)
        assertTrue(dailyPick.title.isNotBlank())
        assertTrue(dailyPick.artistDisplayName.isNotBlank())
        assertTrue(dailyPick.bestDisplayImage.startsWith("http"))
    }

    @Test
    fun `favorite toggle persists artwork record into Room database`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = DailyCuratorDatabase.getInstance(context)
        val repository = CuratorRepository(db.artworkDao(), db.artworkCacheDao())

        val testArtwork = CuratedMasterpieces.allMasterpieces.first()

        // 1. Initially not favorite
        var isFav = repository.isFavorite(testArtwork.id).first()
        assertFalse(isFav)

        // 2. Toggle favorite (Insert into Room)
        repository.toggleFavorite(testArtwork)

        isFav = repository.isFavorite(testArtwork.id).first()
        assertTrue(isFav)

        val favoritesList = repository.favorites.first()
        assertTrue(favoritesList.any { it.id == testArtwork.id })

        // 3. Toggle again (Delete from Room)
        repository.toggleFavorite(testArtwork)

        isFav = repository.isFavorite(testArtwork.id).first()
        assertFalse(isFav)
    }

    @Test
    fun `wallpaper helper sets bitmap for home, lock, and both targets successfully`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testBitmap = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)

        // Test HOME_SCREEN
        val homeResult = com.example.data.wallpaper.WallpaperHelper.setWallpaper(
            context,
            testBitmap,
            com.example.data.wallpaper.WallpaperTarget.HOME_SCREEN
        )
        assertTrue(homeResult.isSuccess)

        // Test LOCK_SCREEN
        val lockResult = com.example.data.wallpaper.WallpaperHelper.setWallpaper(
            context,
            testBitmap,
            com.example.data.wallpaper.WallpaperTarget.LOCK_SCREEN
        )
        assertTrue(lockResult.isSuccess)

        // Test BOTH
        val bothResult = com.example.data.wallpaper.WallpaperHelper.setWallpaper(
            context,
            testBitmap,
            com.example.data.wallpaper.WallpaperTarget.BOTH
        )
        assertTrue(bothResult.isSuccess)
    }

    @Test
    fun `work manager daily scheduler executes without throwing exception`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = androidx.work.Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor())
            .build()
        try {
            androidx.work.WorkManager.initialize(context, config)
        } catch (_: Exception) {
            // Already initialized
        }
        
        var scheduledSuccessfully = false
        try {
            com.example.work.DailyWorkScheduler.scheduleDailyNotifications(context)
            scheduledSuccessfully = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        assertTrue(scheduledSuccessfully)
    }

    @Test
    fun `share artwork text formatting includes title, artist, and image URL`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testArtwork = com.example.data.repository.CuratedMasterpieces.allMasterpieces.first()
        val shareText = context.getString(
            R.string.share_text_format,
            testArtwork.title,
            testArtwork.artistDisplayName,
            testArtwork.objectDate,
            testArtwork.bestDisplayImage
        )

        assertTrue(shareText.contains(testArtwork.title))
        assertTrue(shareText.contains(testArtwork.artistDisplayName))
        assertTrue(shareText.contains(testArtwork.bestDisplayImage))
    }

    @Test
    fun `network client initializes cache and apis successfully`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.data.remote.NetworkClient.init(context)
        val client = com.example.data.remote.NetworkClient.okHttpClient
        assertNotNull(client)
        assertNotNull(client.cache)
        assertNotNull(com.example.data.remote.NetworkClient.artworkApiService)
        assertNotNull(com.example.data.remote.NetworkClient.metApi)
        assertNotNull(com.example.data.remote.NetworkClient.articApi)
    }
}

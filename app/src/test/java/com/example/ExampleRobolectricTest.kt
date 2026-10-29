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
}

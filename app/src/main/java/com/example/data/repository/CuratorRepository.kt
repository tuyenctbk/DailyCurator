package com.example.data.repository

import com.example.data.local.ArtworkCacheDao
import com.example.data.local.ArtworkDao
import com.example.data.local.ArtworkEntity
import com.example.data.local.CachedArtworkEntity
import com.example.data.model.Artwork
import com.example.data.remote.ArtworkApiService
import com.example.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Unified Repository coordinating between Retrofit network service (ArtworkApiService)
 * and Room DAOs (ArtworkDao and ArtworkCacheDao) for daily artwork data, gallery archives,
 * and persistent favorites.
 */
class CuratorRepository(
    private val artworkDao: ArtworkDao,
    private val artworkCacheDao: ArtworkCacheDao,
    private val apiService: ArtworkApiService = NetworkClient.artworkApiService
) {
    // Flow of bookmarked favorites from Room
    val favorites: Flow<List<Artwork>> = artworkDao.getAllFavorites().map { entities ->
        entities.map { it.toArtwork() }
    }

    // Flow of recently cached artwork metadata from Room
    val recentCachedArtworks: Flow<List<Artwork>> = artworkCacheDao.getRecentCachedArtworks().map { entities ->
        entities.map { it.toArtwork() }
    }

    private val _dailyArtworkState = MutableStateFlow(CuratedMasterpieces.getDailyCuratorPick())

    /**
     * Unified reactive Flow emitting the current Daily Artwork, combining local seed,
     * Room DAO cache, and favorite bookmark status.
     */
    val dailyArtworkFlow: Flow<Artwork> = combine(_dailyArtworkState, favorites) { daily, favs ->
        val isFav = favs.any { it.id == daily.id }
        daily.copy(isFavorite = isFav)
    }

    fun isFavorite(id: String): Flow<Boolean> = artworkDao.isFavorite(id)

    suspend fun toggleFavorite(artwork: Artwork) = withContext(Dispatchers.IO) {
        val existing = artworkDao.getArtworkById(artwork.id)
        if (existing != null) {
            artworkDao.deleteFavoriteById(artwork.id)
        } else {
            artworkDao.insertFavorite(ArtworkEntity.fromArtwork(artwork))
        }
    }

    fun getDailyArtwork(): Artwork {
        return _dailyArtworkState.value
    }

    fun setDailyArtwork(artwork: Artwork) {
        _dailyArtworkState.value = artwork.copy(isDailyPick = true)
    }

    /**
     * Refreshes the daily artwork by fetching a fresh public-domain masterwork
     * from The Met or Art Institute of Chicago APIs via Retrofit, caching it in Room.
     */
    suspend fun refreshDailyArtwork(): Result<Artwork> = withContext(Dispatchers.IO) {
        try {
            // Attempt to fetch a highlight object from The Met
            val metResponse = apiService.searchMetObjects(query = "masterpiece", isPublicDomain = true)
            val objectIds = metResponse.objectIDs?.filter { it > 0 } ?: emptyList()

            if (objectIds.isNotEmpty()) {
                val randomId = objectIds.shuffled().first()
                val metObj = apiService.getMetObject(randomId)

                if (!metObj.primaryImage.isNullOrBlank() || !metObj.primaryImageSmall.isNullOrBlank()) {
                    val artwork = metObj.toArtwork().copy(isDailyPick = true)
                    // Cache in Room DAO
                    artworkCacheDao.insertCachedArtwork(CachedArtworkEntity.fromArtwork(artwork))
                    _dailyArtworkState.value = artwork
                    return@withContext Result.success(artwork)
                }
            }

            // Fallback to local curated masterwork rotation
            val seedPick = CuratedMasterpieces.getDailyCuratorPick()
            _dailyArtworkState.value = seedPick
            Result.success(seedPick)
        } catch (e: Exception) {
            // On network failure, preserve offline pick
            val seedPick = CuratedMasterpieces.getDailyCuratorPick()
            _dailyArtworkState.value = seedPick
            Result.success(seedPick)
        }
    }

    fun getAllCurated(): List<Artwork> {
        return CuratedMasterpieces.allMasterpieces
    }

    /**
     * Coordinates between local seed masterworks, Room database cache, and Retrofit network calls
     * to return a unified list of artworks matching search and movement filters.
     */
    suspend fun searchArtworks(
        query: String,
        selectedMovement: String = "All",
        selectedEra: String = "All"
    ): List<Artwork> = withContext(Dispatchers.IO) {
        val seed = CuratedMasterpieces.allMasterpieces

        val localSeedFiltered = seed.filter { artwork ->
            val matchesQuery = query.isBlank() ||
                artwork.title.contains(query, ignoreCase = true) ||
                artwork.artistDisplayName.contains(query, ignoreCase = true) ||
                artwork.movement.contains(query, ignoreCase = true) ||
                artwork.medium.contains(query, ignoreCase = true) ||
                artwork.curatorNote.contains(query, ignoreCase = true)

            val matchesMovement = selectedMovement == "All" ||
                artwork.movement.equals(selectedMovement, ignoreCase = true)

            val matchesEra = selectedEra == "All" ||
                artwork.era.contains(selectedEra, ignoreCase = true)

            matchesQuery && matchesMovement && matchesEra
        }

        // Query Room DAO cache for previously fetched artworks
        val roomCachedArtworks = if (query.isNotBlank()) {
            artworkCacheDao.searchCachedArtworks(query).map { it.toArtwork() }
        } else {
            emptyList()
        }

        val currentLocalCombined = (localSeedFiltered + roomCachedArtworks).distinctBy { it.id }

        // Fetch live metadata from Retrofit service if searching specific query
        if (query.isNotBlank() && selectedMovement == "All" && selectedEra == "All") {
            val liveFetchedArtworks = mutableListOf<Artwork>()

            // 1. Query Metropolitan Museum of Art API
            try {
                val metResponse = apiService.searchMetObjects(query = query)
                val objectIds = metResponse.objectIDs?.take(6) ?: emptyList()

                for (oid in objectIds) {
                    try {
                        val metObj = apiService.getMetObject(oid)
                        if (!metObj.primaryImage.isNullOrBlank() || !metObj.primaryImageSmall.isNullOrBlank()) {
                            liveFetchedArtworks.add(metObj.toArtwork())
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}

            // 2. Query Art Institute of Chicago API
            try {
                val articResponse = apiService.searchArticArtworks(query = query, limit = 6)
                val iiifBase = articResponse.config?.iiifUrl ?: "https://www.artic.edu/iiif/2"
                articResponse.data?.forEach { item ->
                    if (!item.imageId.isNullOrBlank()) {
                        liveFetchedArtworks.add(item.toArtwork(iiifBase))
                    }
                }
            } catch (_: Exception) {}

            // Save remote results in Room DAO cache for offline availability
            if (liveFetchedArtworks.isNotEmpty()) {
                val entitiesToCache = liveFetchedArtworks.map { CachedArtworkEntity.fromArtwork(it) }
                artworkCacheDao.insertCachedArtworks(entitiesToCache)

                return@withContext (currentLocalCombined + liveFetchedArtworks).distinctBy { it.id }
            }
        }

        currentLocalCombined
    }
}

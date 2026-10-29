package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtworkCacheDao {

    @Query("SELECT * FROM cached_artworks ORDER BY cachedAt DESC LIMIT :limit")
    fun getRecentCachedArtworks(limit: Int = 100): Flow<List<CachedArtworkEntity>>

    @Query("SELECT * FROM cached_artworks WHERE id = :id LIMIT 1")
    suspend fun getCachedArtworkById(id: String): CachedArtworkEntity?

    @Query("SELECT * FROM cached_artworks WHERE title LIKE '%' || :query || '%' OR artistDisplayName LIKE '%' || :query || '%' OR movement LIKE '%' || :query || '%' ORDER BY cachedAt DESC")
    suspend fun searchCachedArtworks(query: String): List<CachedArtworkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedArtwork(artwork: CachedArtworkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedArtworks(artworks: List<CachedArtworkEntity>)

    @Query("DELETE FROM cached_artworks WHERE cachedAt < :thresholdTime")
    suspend fun deleteOldCache(thresholdTime: Long)

    @Query("DELETE FROM cached_artworks")
    suspend fun clearCache()
}

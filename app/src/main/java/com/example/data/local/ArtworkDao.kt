package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtworkDao {
    @Query("SELECT * FROM favorite_artworks ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<ArtworkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_artworks WHERE id = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Query("SELECT * FROM favorite_artworks WHERE id = :id LIMIT 1")
    suspend fun getArtworkById(id: String): ArtworkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(artwork: ArtworkEntity)

    @Query("DELETE FROM favorite_artworks WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)
}

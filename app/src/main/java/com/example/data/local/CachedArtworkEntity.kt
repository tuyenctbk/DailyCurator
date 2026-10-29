package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Artwork

@Entity(tableName = "cached_artworks")
data class CachedArtworkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistDisplayName: String,
    val artistDisplayBio: String = "",
    val objectDate: String = "",
    val medium: String = "",
    val dimensions: String = "",
    val creditLine: String = "",
    val department: String = "",
    val movement: String = "Fine Art",
    val era: String = "",
    val primaryImage: String = "",
    val primaryImageSmall: String = "",
    val sourceMuseum: String = "",
    val curatorNote: String = "",
    val isPublicDomain: Boolean = true,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toArtwork(isFavorite: Boolean = false): Artwork {
        return Artwork(
            id = id,
            title = title,
            artistDisplayName = artistDisplayName,
            artistDisplayBio = artistDisplayBio,
            objectDate = objectDate,
            medium = medium,
            dimensions = dimensions,
            creditLine = creditLine,
            department = department,
            movement = movement,
            era = era,
            primaryImage = primaryImage,
            primaryImageSmall = primaryImageSmall,
            sourceMuseum = sourceMuseum,
            curatorNote = curatorNote,
            isPublicDomain = isPublicDomain,
            isFavorite = isFavorite
        )
    }

    companion object {
        fun fromArtwork(artwork: Artwork): CachedArtworkEntity {
            return CachedArtworkEntity(
                id = artwork.id,
                title = artwork.title,
                artistDisplayName = artwork.artistDisplayName,
                artistDisplayBio = artwork.artistDisplayBio,
                objectDate = artwork.objectDate,
                medium = artwork.medium,
                dimensions = artwork.dimensions,
                creditLine = artwork.creditLine,
                department = artwork.department,
                movement = artwork.movement,
                era = artwork.era,
                primaryImage = artwork.primaryImage,
                primaryImageSmall = artwork.primaryImageSmall,
                sourceMuseum = artwork.sourceMuseum,
                curatorNote = artwork.curatorNote,
                isPublicDomain = artwork.isPublicDomain
            )
        }
    }
}

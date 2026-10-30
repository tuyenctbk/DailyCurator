package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Artwork

/**
 * Room Entity storing favorited fine art pieces with complete metadata:
 * title, artist, image URL (both master and thumbnail), date discovered,
 * museum provenance, and historical curatorial analysis.
 */
@Entity(tableName = "favorite_artworks")
data class ArtworkEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "artist_display_name")
    val artistDisplayName: String,

    @ColumnInfo(name = "artist_display_bio")
    val artistDisplayBio: String = "",

    @ColumnInfo(name = "object_date")
    val objectDate: String = "",

    @ColumnInfo(name = "medium")
    val medium: String = "",

    @ColumnInfo(name = "dimensions")
    val dimensions: String = "",

    @ColumnInfo(name = "credit_line")
    val creditLine: String = "",

    @ColumnInfo(name = "department")
    val department: String = "",

    @ColumnInfo(name = "movement")
    val movement: String = "Fine Art",

    @ColumnInfo(name = "era")
    val era: String = "",

    @ColumnInfo(name = "primary_image")
    val primaryImage: String = "",

    @ColumnInfo(name = "primary_image_small")
    val primaryImageSmall: String = "",

    @ColumnInfo(name = "source_museum")
    val sourceMuseum: String = "",

    @ColumnInfo(name = "curator_note")
    val curatorNote: String = "",

    @ColumnInfo(name = "date_discovered")
    val dateDiscovered: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "saved_at")
    val savedAt: Long = dateDiscovered
) {
    // Convenient getters matching schema requirements
    val artist: String get() = artistDisplayName
    val imageUrl: String get() = if (primaryImage.isNotBlank()) primaryImage else primaryImageSmall

    fun toArtwork(): Artwork {
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
            isFavorite = true,
            dateDiscovered = dateDiscovered
        )
    }

    companion object {
        fun fromArtwork(artwork: Artwork): ArtworkEntity {
            return ArtworkEntity(
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
                dateDiscovered = artwork.dateDiscovered,
                savedAt = artwork.dateDiscovered
            )
        }
    }
}

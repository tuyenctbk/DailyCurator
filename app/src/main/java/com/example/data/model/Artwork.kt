package com.example.data.model

data class Artwork(
    val id: String,
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
    val sourceMuseum: String = "The Metropolitan Museum of Art",
    val curatorNote: String = "",
    val isPublicDomain: Boolean = true,
    val isDailyPick: Boolean = false,
    val isFavorite: Boolean = false,
    val dateDiscovered: Long = System.currentTimeMillis()
) {
    // Convenient aliases matching metadata queries
    val artist: String
        get() = artistDisplayName

    val imageUrl: String
        get() = bestDisplayImage

    val bestDisplayImage: String
        get() = if (primaryImage.isNotBlank()) primaryImage else primaryImageSmall

    val thumbnailImage: String
        get() = if (primaryImageSmall.isNotBlank()) primaryImageSmall else primaryImage
}

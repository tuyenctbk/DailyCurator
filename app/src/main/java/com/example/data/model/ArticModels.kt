package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Art Institute of Chicago API Models
 * Base URL: https://api.artic.edu/api/v1/
 */

@JsonClass(generateAdapter = true)
data class ArticSearchResponse(
    @Json(name = "data") val data: List<ArticArtworkData>? = null,
    @Json(name = "pagination") val pagination: ArticPagination? = null,
    @Json(name = "config") val config: ArticConfig? = null
)

@JsonClass(generateAdapter = true)
data class ArticDetailResponse(
    @Json(name = "data") val data: ArticArtworkData? = null,
    @Json(name = "config") val config: ArticConfig? = null
)

@JsonClass(generateAdapter = true)
data class ArticArtworkData(
    @Json(name = "id") val id: Long? = 0,
    @Json(name = "title") val title: String? = null,
    @Json(name = "artist_display") val artistDisplay: String? = null,
    @Json(name = "artist_title") val artistTitle: String? = null,
    @Json(name = "date_display") val dateDisplay: String? = null,
    @Json(name = "date_start") val dateStart: Int? = null,
    @Json(name = "date_end") val dateEnd: Int? = null,
    @Json(name = "medium_display") val mediumDisplay: String? = null,
    @Json(name = "dimensions") val dimensions: String? = null,
    @Json(name = "credit_line") val creditLine: String? = null,
    @Json(name = "image_id") val imageId: String? = null,
    @Json(name = "is_public_domain") val isPublicDomain: Boolean? = true,
    @Json(name = "department_title") val departmentTitle: String? = null,
    @Json(name = "style_title") val styleTitle: String? = null,
    @Json(name = "classification_title") val classificationTitle: String? = null,
    @Json(name = "place_of_origin") val placeOfOrigin: String? = null,
    @Json(name = "thumbnail") val thumbnail: ArticThumbnail? = null
) {
    /**
     * Resolves IIIF image URLs for standard viewing and uncompressed 4K master files
     */
    fun getFullImageUrl(iiifBaseUrl: String = "https://www.artic.edu/iiif/2"): String? {
        val imgId = imageId ?: return null
        return "$iiifBaseUrl/$imgId/full/1686,/0/default.jpg"
    }

    fun getThumbnailUrl(iiifBaseUrl: String = "https://www.artic.edu/iiif/2"): String? {
        val imgId = imageId ?: return null
        return "$iiifBaseUrl/$imgId/full/843,/0/default.jpg"
    }

    fun toArtwork(iiifBaseUrl: String = "https://www.artic.edu/iiif/2"): Artwork {
        val fullUrl = getFullImageUrl(iiifBaseUrl) ?: ""
        val thumbUrl = getThumbnailUrl(iiifBaseUrl) ?: fullUrl

        val artistName = artistTitle?.takeIf { it.isNotBlank() }
            ?: artistDisplay?.substringBefore("\n")?.trim()
            ?: "Unknown Artist"

        val movementStr = styleTitle ?: classificationTitle ?: departmentTitle ?: "Fine Art"

        val note = buildString {
            append("Preserved in the public domain collection of the Art Institute of Chicago (${departmentTitle ?: "Permanent Gallery"}).")
            if (!placeOfOrigin.isNullOrBlank()) append(" Origin: $placeOfOrigin.")
            if (!styleTitle.isNullOrBlank()) append(" Style: $styleTitle.")
            if (!mediumDisplay.isNullOrBlank()) append(" Medium: $mediumDisplay.")
        }

        return Artwork(
            id = "artic-${id ?: 0}",
            title = title?.ifBlank { "Untitled" } ?: "Untitled",
            artistDisplayName = artistName,
            artistDisplayBio = artistDisplay ?: "",
            objectDate = dateDisplay ?: "",
            medium = mediumDisplay ?: "",
            dimensions = dimensions ?: "",
            creditLine = creditLine ?: "",
            department = departmentTitle ?: "Arts of the World",
            movement = movementStr,
            era = dateDisplay ?: "",
            primaryImage = fullUrl,
            primaryImageSmall = thumbUrl,
            sourceMuseum = "Art Institute of Chicago",
            curatorNote = note,
            isPublicDomain = isPublicDomain ?: true
        )
    }
}

@JsonClass(generateAdapter = true)
data class ArticThumbnail(
    @Json(name = "lqip") val lqip: String? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "height") val height: Int? = null,
    @Json(name = "alt_text") val altText: String? = null
)

@JsonClass(generateAdapter = true)
data class ArticConfig(
    @Json(name = "iiif_url") val iiifUrl: String? = "https://www.artic.edu/iiif/2",
    @Json(name = "website_url") val websiteUrl: String? = "https://www.artic.edu"
)

@JsonClass(generateAdapter = true)
data class ArticPagination(
    @Json(name = "total") val total: Int? = 0,
    @Json(name = "limit") val limit: Int? = 0,
    @Json(name = "offset") val offset: Int? = 0,
    @Json(name = "total_pages") val totalPages: Int? = 0,
    @Json(name = "current_page") val currentPage: Int? = 0
)

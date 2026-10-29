package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Metropolitan Museum of Art API Models
 * Base URL: https://collectionapi.metmuseum.org/public/collection/v1/
 */

@JsonClass(generateAdapter = true)
data class MetSearchResponse(
    @Json(name = "total") val total: Int? = 0,
    @Json(name = "objectIDs") val objectIDs: List<Long>? = null
)

@JsonClass(generateAdapter = true)
data class MetObjectResponse(
    @Json(name = "objectID") val objectID: Long = 0,
    @Json(name = "isHighlight") val isHighlight: Boolean? = false,
    @Json(name = "isPublicDomain") val isPublicDomain: Boolean? = true,
    @Json(name = "primaryImage") val primaryImage: String? = null,
    @Json(name = "primaryImageSmall") val primaryImageSmall: String? = null,
    @Json(name = "additionalImages") val additionalImages: List<String>? = null,
    @Json(name = "department") val department: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "culture") val culture: String? = null,
    @Json(name = "period") val period: String? = null,
    @Json(name = "dynasty") val dynasty: String? = null,
    @Json(name = "reign") val reign: String? = null,
    @Json(name = "artistDisplayName") val artistDisplayName: String? = null,
    @Json(name = "artistDisplayBio") val artistDisplayBio: String? = null,
    @Json(name = "artistNationality") val artistNationality: String? = null,
    @Json(name = "artistBeginDate") val artistBeginDate: String? = null,
    @Json(name = "artistEndDate") val artistEndDate: String? = null,
    @Json(name = "objectDate") val objectDate: String? = null,
    @Json(name = "objectBeginDate") val objectBeginDate: Int? = null,
    @Json(name = "objectEndDate") val objectEndDate: Int? = null,
    @Json(name = "medium") val medium: String? = null,
    @Json(name = "dimensions") val dimensions: String? = null,
    @Json(name = "creditLine") val creditLine: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "state") val state: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "region") val region: String? = null,
    @Json(name = "classification") val classification: String? = null,
    @Json(name = "rightsAndReproduction") val rightsAndReproduction: String? = null,
    @Json(name = "objectURL") val objectURL: String? = null,
    @Json(name = "tags") val tags: List<MetTag>? = null
) {
    fun toArtwork(): Artwork {
        val displayImg = primaryImage ?: primaryImageSmall ?: ""
        val smallImg = primaryImageSmall ?: displayImg
        val artistBioStr = buildString {
            if (!artistDisplayBio.isNullOrBlank()) {
                append(artistDisplayBio)
            } else if (!artistNationality.isNullOrBlank()) {
                append(artistNationality)
                if (!artistBeginDate.isNullOrBlank() || !artistEndDate.isNullOrBlank()) {
                    append(", ${artistBeginDate ?: ""}–${artistEndDate ?: ""}")
                }
            }
        }

        val movementStr = period ?: culture ?: department ?: "Metropolitan Collection"

        val note = buildString {
            append("An open-access public domain masterwork from The Metropolitan Museum of Art (${department ?: "Permanent Collection"}).")
            if (!culture.isNullOrBlank()) append(" Culture: $culture.")
            if (!period.isNullOrBlank()) append(" Period: $period.")
            if (!medium.isNullOrBlank()) append(" Rendered in $medium.")
            append(" Preserved under Creative Commons Zero (CC0).")
        }

        return Artwork(
            id = "met-$objectID",
            title = title?.ifBlank { "Untitled" } ?: "Untitled",
            artistDisplayName = artistDisplayName?.ifBlank { "Unknown Artist" } ?: "Unknown Artist",
            artistDisplayBio = artistBioStr,
            objectDate = objectDate ?: "",
            medium = medium ?: "",
            dimensions = dimensions ?: "",
            creditLine = creditLine ?: "",
            department = department ?: "European Paintings",
            movement = movementStr,
            era = period ?: "",
            primaryImage = displayImg,
            primaryImageSmall = smallImg,
            sourceMuseum = "The Metropolitan Museum of Art",
            curatorNote = note,
            isPublicDomain = isPublicDomain ?: true
        )
    }
}

@JsonClass(generateAdapter = true)
data class MetTag(
    @Json(name = "term") val term: String? = null,
    @Json(name = "AAT_URL") val aatUrl: String? = null,
    @Json(name = "Wikidata_URL") val wikidataUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class MetDepartmentsResponse(
    @Json(name = "departments") val departments: List<MetDepartment>? = null
)

@JsonClass(generateAdapter = true)
data class MetDepartment(
    @Json(name = "departmentId") val departmentId: Int = 0,
    @Json(name = "displayName") val displayName: String = ""
)

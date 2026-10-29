package com.example.data.remote

import com.example.data.model.ArticDetailResponse
import com.example.data.model.ArticSearchResponse
import com.example.data.model.MetDepartmentsResponse
import com.example.data.model.MetObjectResponse
import com.example.data.model.MetSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Unified Retrofit service interface for fetching artwork metadata from:
 * 1. The Metropolitan Museum of Art Collection API (https://collectionapi.metmuseum.org/public/collection/v1/)
 * 2. The Art Institute of Chicago API (https://api.artic.edu/api/v1/)
 */
interface ArtworkApiService {

    // =========================================================================
    // 1. Metropolitan Museum of Art API Endpoints
    // =========================================================================

    /**
     * Retrieves full catalog metadata for a single artwork object by its Met ID.
     */
    @GET("https://collectionapi.metmuseum.org/public/collection/v1/objects/{objectID}")
    suspend fun getMetObject(
        @Path("objectID") objectId: Long
    ): MetObjectResponse

    /**
     * Searches The Met's open-access collection by query string, filtered to public domain items with images.
     */
    @GET("https://collectionapi.metmuseum.org/public/collection/v1/search")
    suspend fun searchMetObjects(
        @Query("q") query: String,
        @Query("hasImages") hasImages: Boolean = true,
        @Query("isPublicDomain") isPublicDomain: Boolean = true,
        @Query("departmentId") departmentId: Int? = null,
        @Query("artistOrCulture") artistOrCulture: Boolean? = null
    ): MetSearchResponse

    /**
     * Retrieves a listing of all curatorial departments at The Met.
     */
    @GET("https://collectionapi.metmuseum.org/public/collection/v1/departments")
    suspend fun getMetDepartments(): MetDepartmentsResponse


    // =========================================================================
    // 2. Art Institute of Chicago (AIC) API Endpoints
    // =========================================================================

    /**
     * Retrieves a paginated list of artworks from the Art Institute of Chicago collection.
     */
    @GET("https://api.artic.edu/api/v1/artworks")
    suspend fun getArticArtworks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = DEFAULT_ARTIC_FIELDS
    ): ArticSearchResponse

    /**
     * Searches the Art Institute of Chicago collection by keyword, title, or artist.
     */
    @GET("https://api.artic.edu/api/v1/artworks/search")
    suspend fun searchArticArtworks(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = DEFAULT_ARTIC_FIELDS
    ): ArticSearchResponse

    /**
     * Retrieves rich detail metadata for a specific artwork at the Art Institute of Chicago.
     */
    @GET("https://api.artic.edu/api/v1/artworks/{id}")
    suspend fun getArticArtworkById(
        @Path("id") id: Long,
        @Query("fields") fields: String = DEFAULT_ARTIC_FIELDS
    ): ArticDetailResponse


    // =========================================================================
    // 3. Dynamic Full URL Query Endpoints
    // =========================================================================

    @GET
    suspend fun getMetObjectByUrl(@Url url: String): MetObjectResponse

    @GET
    suspend fun getArticSearchByUrl(@Url url: String): ArticSearchResponse

    companion object {
        const val MET_BASE_URL = "https://collectionapi.metmuseum.org/public/collection/v1/"
        const val ARTIC_BASE_URL = "https://api.artic.edu/api/v1/"
        const val DEFAULT_ARTIC_FIELDS =
            "id,title,artist_display,artist_title,date_display,medium_display,dimensions,credit_line,image_id,is_public_domain,department_title,style_title,classification_title,place_of_origin,thumbnail"
    }
}

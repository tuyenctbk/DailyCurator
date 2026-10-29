package com.example.data.remote

import com.example.data.model.ArticSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ArticApi {
    @GET("artworks")
    suspend fun getArtworks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "id,title,artist_display,date_display,medium_display,dimensions,credit_line,image_id,is_public_domain,department_title,style_title,place_of_origin"
    ): ArticSearchResponse

    @GET("artworks/search")
    suspend fun searchArtworks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "id,title,artist_display,date_display,medium_display,dimensions,credit_line,image_id,is_public_domain,department_title,style_title,place_of_origin"
    ): ArticSearchResponse
}

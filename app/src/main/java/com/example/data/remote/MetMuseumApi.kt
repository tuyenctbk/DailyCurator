package com.example.data.remote

import com.example.data.model.MetObjectResponse
import com.example.data.model.MetSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MetMuseumApi {
    @GET("objects/{objectID}")
    suspend fun getObject(@Path("objectID") objectId: Long): MetObjectResponse

    @GET("search")
    suspend fun searchObjects(
        @Query("q") query: String,
        @Query("hasImages") hasImages: Boolean = true,
        @Query("isPublicDomain") isPublicDomain: Boolean = true
    ): MetSearchResponse
}

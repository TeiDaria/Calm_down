package com.example.calmdown

import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApi {
    @GET("/search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("client_id") clientId: String
    ): SearchResponse
}
package com.example.calmdown

import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApi {
    @GET("photos")
    suspend fun getPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("client_id") clientId: String // Ваш Access Key
    ): List<Photo>
}
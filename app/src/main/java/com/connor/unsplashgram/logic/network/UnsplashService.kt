package com.connor.unsplashgram.logic.network

import com.connor.unsplashgram.logic.model.UnsplashPhoto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashService {

    @GET("photos")
    fun loadPhotos(
        @Query("client_id") clientId: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int
        ): Call<List<UnsplashPhoto>>
}
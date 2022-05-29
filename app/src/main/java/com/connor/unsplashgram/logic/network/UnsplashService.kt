package com.connor.unsplashgram.logic.network

import com.connor.unsplashgram.logic.model.SearchResponse
import com.connor.unsplashgram.logic.model.UnsplashPhoto
import com.connor.unsplashgram.logic.model.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UnsplashService {

    @GET("search/photos")
    fun searchPhotos(
        @Query("client_id") clientId: String,
        @Query("query") criteria: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int,
        @Query("order_by") order_by: String?,
        @Query("orientation") orientation: String?
    ): Call<SearchResponse>

    @GET("photos")
    fun loadPhotos(
        @Query("client_id") clientId: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int,
        @Query("order_by") order_by: String?
        ): Call<List<UnsplashPhoto>>

    @GET("photos/{id}")
    fun getPhoto(
        @Path("id") id: String,
        @Query("client_id") clientId: String
    ): Call<UnsplashPhoto>

    @GET("users/{username}")
    fun getUserProfile(
        @Path("username") username: String,
        @Query("client_id") clientId: String
    ): Call<User>

    @GET("users/{username}/photos")
    fun getUserPhotos(
        @Path("username") username: String,
        @Query("client_id") clientId: String,
        @Query("page") page: Int?,
        @Query("per_page") per_page: Int?
    ): Call<List<UnsplashPhoto>>
}
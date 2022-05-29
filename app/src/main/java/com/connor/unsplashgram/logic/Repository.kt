package com.connor.unsplashgram.logic

import android.util.Log
import android.widget.Toast
import kotlin.coroutines.CoroutineContext
import androidx.lifecycle.liveData
import com.connor.unsplashgram.App
import com.connor.unsplashgram.logic.model.UnsplashPhoto
import com.connor.unsplashgram.logic.network.UnsplashNetwork
import kotlinx.coroutines.Dispatchers

object Repository {

    fun loadPhotos(clientId: String, page: Int, pageSize: Int, orderBy: String) = fire(Dispatchers.IO) {
        Log.d(App.TAG, "loadPhotos: begin")
        val photosResponse = UnsplashNetwork.loadPhotos(clientId, page, pageSize, orderBy)
        if (photosResponse[1].id.isNotEmpty()) {
            val photos = photosResponse
            //Log.d(App.TAG, "Repository: loadPhotos: $photos")
            Result.success(photos)
        } else {
            Log.d(App.TAG, "Repository: loadPhotos: $photosResponse")
            Result.failure(RuntimeException("response status is ${photosResponse[1]}"))
        }
    }

    fun searchPhotos(clientId: String, criteria: String, page: Int, pageSize: Int, orderBy: String, orientation: String?) = fire(Dispatchers.IO) {
        Log.d(App.TAG, "searchPhotos: begin")
        val searchResponse = UnsplashNetwork.searchPhotos(clientId, criteria, page, pageSize, orderBy, orientation)
        if (searchResponse.results[1].id.isNotEmpty()) {
            val result = searchResponse
           // Log.d(App.TAG, "Repository: searchPhotos: $result")
            Result.success(result)
        } else {
            Log.d(App.TAG, "Repository: searchPhotos: $searchResponse")
            Result.failure(java.lang.RuntimeException("response status is $searchResponse"))
        }
    }

    fun getPhoto(id: String, clientId: String) = fire(Dispatchers.IO) {
        Log.d(App.TAG, "getPhotos: begin")
        val getResponse = UnsplashNetwork.getPhoto(id, clientId)
        if (getResponse.id.isNotEmpty()) {
            val photo = getResponse
         //   Log.d(App.TAG, "Repository: getPhoto: $photo")
            Result.success(photo)
        } else {
            Log.d(App.TAG, "Repository: getResponse: $getResponse")
            Result.failure(java.lang.RuntimeException("response status is $getResponse"))
        }
    }

    fun getUserProfile(username: String, clientId: String) = fire(Dispatchers.IO) {
        Log.d(App.TAG, "getUserProfile: begin")
        val userProfileResponse = UnsplashNetwork.getUserProfile(username, clientId)
        if (userProfileResponse.id.isNotEmpty()) {
            val userProfile = userProfileResponse
         //   Log.d(App.TAG, "Repository: getUserProfile: $userProfile")
            Result.success(userProfile)
        } else {
            Log.d(App.TAG, "Repository: getUserProfile: $userProfileResponse")
            Result.failure(java.lang.RuntimeException("response status is $userProfileResponse"))
        }
    }

    fun getUserPhotos(username: String, clientId: String, page: Int, pageSize: Int) = fire(Dispatchers.IO) {
        Log.d(App.TAG, "getUserPhotos: begin")
        val userPhotosResponse = UnsplashNetwork.getUserPhotos(username, clientId, page, pageSize)
        if (userPhotosResponse.isNotEmpty()) {
            val userPhotos = userPhotosResponse
         //   Log.d(App.TAG, "Repository: getUserPhotos: $userPhotos")
            Result.success(userPhotos)
        } else {
            Log.d(App.TAG, "Repository: getUserPhotos: $userPhotosResponse")
            Result.failure(java.lang.RuntimeException("response status is $userPhotosResponse"))
        }
    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Log.d(App.TAG, "fire: $e")
                Result.failure<T>(e)
            }
            emit(result)
        }
}
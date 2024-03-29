package com.connor.unsplashgram.logic.network

import android.util.Log
import android.widget.Toast
import com.connor.unsplashgram.App
import com.connor.unsplashgram.App.Companion.TAG
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object UnsplashNetwork {

    private val UnsplashService = ServiceCreator.create<UnsplashService>()

    suspend fun loadPhotos(clientId: String, page: Int, pageSize: Int, orderBy: String) =
        UnsplashService.loadPhotos(clientId, page, pageSize, orderBy).await()

    suspend fun searchPhotos(
        clientId: String, criteria: String, page: Int, pageSize: Int, orderBy: String, orientation: String?
    ) = UnsplashService.searchPhotos(clientId, criteria, page, pageSize, orderBy, orientation).await()

    suspend fun getPhoto(id: String, clientId: String) =
        UnsplashService.getPhoto(id, clientId).await()

    suspend fun getUserProfile(username: String, clientId: String) =
        UnsplashService.getUserProfile(username, clientId).await()

    suspend fun getUserPhotos(username: String, clientId: String, page: Int, pageSize: Int) =
        UnsplashService.getUserPhotos(username, clientId, page, pageSize).await()


    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine {
            enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    Log.d(TAG, "UnsplashNetwork: onFailure")
                    it.resumeWithException(t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) {
                        it.resume(body)
                        Log.d(TAG, "onResponse: body != null")
                    } else {
                        it.resumeWithException(RuntimeException("response body is null"))
                        Log.d(TAG, "onResponse: response body is null")
                    }
                }
            })
        }
    }
}
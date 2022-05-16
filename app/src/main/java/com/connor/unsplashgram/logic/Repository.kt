package com.connor.unsplashgram.logic

import android.util.Log
import android.widget.Toast
import kotlin.coroutines.CoroutineContext
import androidx.lifecycle.liveData
import com.connor.unsplashgram.App
import com.connor.unsplashgram.logic.network.UnsplashNetwork
import kotlinx.coroutines.Dispatchers

object Repository {

    fun loadPhotos(clientId: String, page: Int, pageSize: Int) = fire(Dispatchers.IO) {
        Log.d(App.TAG, "loadPhotos: begin")
        val photosResponse = UnsplashNetwork.loadPhotos(clientId, page, pageSize)
        if (photosResponse[1].id.isNotEmpty()) {
            val photos = photosResponse
            Log.d(App.TAG, "Repository: loadPhotos: $photos")
            Result.success(photos)
        } else {
            Log.d(App.TAG, "Repository: loadPhotos: $photosResponse")
            Result.failure(RuntimeException("response status is ${photosResponse[1]}"))
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
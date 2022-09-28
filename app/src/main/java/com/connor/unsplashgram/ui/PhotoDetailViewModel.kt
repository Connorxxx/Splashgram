package com.connor.unsplashgram.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import com.connor.unsplashgram.App
import com.connor.unsplashgram.logic.Repository
import com.connor.unsplashgram.logic.model.UnsplashPhoto
import kotlin.concurrent.thread

class PhotoDetailViewModel : ViewModel() {

    var id = ""

    val loadList = ArrayList<UnsplashPhoto>()

    val getPhotoLiveData = MutableLiveData<String>()

    val getLiveData = Transformations.switchMap(getPhotoLiveData) {
        Repository.getPhoto(it, App.ACCESS_KEY)
    }

    fun getPhotos(id: String) {
        getPhotoLiveData.value = id
    }
}
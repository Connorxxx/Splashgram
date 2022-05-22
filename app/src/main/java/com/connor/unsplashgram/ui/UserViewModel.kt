package com.connor.unsplashgram.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import com.connor.unsplashgram.App
import com.connor.unsplashgram.logic.Repository
import com.connor.unsplashgram.logic.model.UnsplashPhoto

class UserViewModel : ViewModel() {

    var pageSize = 10
    var page = 1
    var usernameText = ""

    val userPhotosList = ArrayList<UnsplashPhoto>()

    val getUserProfileLiveData = MutableLiveData<String>()

    private val pageLiveData = MutableLiveData<Int>()

    val getUserLiveData = Transformations.switchMap(getUserProfileLiveData) {
        Repository.getUserProfile(it, App.ACCESS_KEY)
    }

    val loadPageLiveData = Transformations.switchMap(pageLiveData) {
        Repository.getUserPhotos(usernameText, App.ACCESS_KEY, it, pageSize)
    }


    fun getUserProfile(username: String) {
        usernameText = username
        getUserProfileLiveData.value = username
    }

    fun loadPage(page: Int) {
        pageLiveData.value = page
    }
}
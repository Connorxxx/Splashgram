package com.connor.unsplashgram.ui

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.connor.unsplashgram.App
import com.connor.unsplashgram.R
import com.connor.unsplashgram.logic.Repository
import com.connor.unsplashgram.logic.model.UnsplashPhoto


class MainViewModel : ViewModel() {

    var pageSize = 20
    var page = 1
    var orderBy = "latest"
    var itemSelected = 0


    private val orderByLiveData = MutableLiveData<String>()
    private val pageLiveData = MutableLiveData<Int>()


    val loadList = ArrayList<UnsplashPhoto>()

    val orderLiveData = Transformations.switchMap(orderByLiveData) {
        Repository.loadPhotos(App.ACCESS_KEY, page, pageSize, it)
    }

    val loadPageLiveData = Transformations.switchMap(pageLiveData) {
        Repository.loadPhotos(App.ACCESS_KEY, it, pageSize, orderBy)
    }

    fun orderPhotos(orderBy: String) {
        page = 1
        orderByLiveData.value = orderBy
    }

    fun loadPage(page: Int) {
        pageLiveData.value = page
    }

}
package com.connor.unsplashgram.ui

import android.content.Context
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.cardview.widget.CardView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.connor.unsplashgram.App
import com.connor.unsplashgram.logic.Repository
import com.connor.unsplashgram.logic.model.UnsplashPhoto
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainViewModel : ViewModel() {

    var pageSize = 20
    var page = 1

    private val collectionsLiveData = MutableLiveData<String>()
    private val pageLiveData = MutableLiveData<Int>()


    val loadList = ArrayList<UnsplashPhoto>()

    val loadLiveData = Transformations.switchMap(collectionsLiveData) {
        Repository.loadPhotos(it, page, pageSize)
    }

    val loadPageLiveData = Transformations.switchMap(pageLiveData) {
        Repository.loadPhotos(App.ACCESS_KEY, it, pageSize)
    }

    fun loadPhotos(clientId: String) {
        collectionsLiveData.value = clientId
    }

    fun loadPage(page: Int) {
        pageLiveData.value = page
    }


}
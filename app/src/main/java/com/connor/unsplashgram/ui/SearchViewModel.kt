package com.connor.unsplashgram.ui

import android.content.Context
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.connor.unsplashgram.App
import com.connor.unsplashgram.logic.Repository
import com.connor.unsplashgram.logic.model.UnsplashPhoto

class SearchViewModel : ViewModel() {

    lateinit var imm: InputMethodManager

    var pageSize = 20
    var page = 1
    var searchText = ""

    private val searchResultLiveData = MutableLiveData<String>()
    private val pageLiveData = MutableLiveData<Int>()

    val searchList = ArrayList<UnsplashPhoto>()

    val searchLiveData = Transformations.switchMap(searchResultLiveData) {
        Repository.searchPhotos(App.ACCESS_KEY, it, page, pageSize)
    }

    val loadPageLiveData = Transformations.switchMap(pageLiveData) {
        Repository.searchPhotos(App.ACCESS_KEY, searchText, it, pageSize)
    }

    fun searchPhotos(criteria: String) {
        searchText = criteria
        searchResultLiveData.value = searchText
    }

    fun loadPage(page: Int) {
        pageLiveData.value = page
    }


    fun actionDone(editText: EditText) {
        Log.d(App.TAG, "searchByUrl: ")
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchWithUrl(editText)
            }
            return@setOnEditorActionListener true
        }
    }

    fun searchByUrl(editText: EditText) {
        imm = editText.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        editText.apply {
            requestFocus()
            imm.showSoftInput(editText, 0)
        }
        if (editText.text.isNotEmpty()) searchWithUrl(editText)
    }

    private fun searchWithUrl(editText: EditText) {
        // swipeRefreshLayout.isRefreshing = true
        hideKeyboard(editText)
        searchPhotos(editText.text.toString())

       // editText.setText("")
    }

    private fun hideKeyboard(editText: EditText) {
        imm = editText.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}
package com.connor.unsplashgram.ui

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.connor.unsplashgram.App
import com.connor.unsplashgram.logic.Repository
import com.connor.unsplashgram.logic.model.UnsplashPhoto
import com.connor.unsplashgram.logic.tools.Tools
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class SearchViewModel : ViewModel() {

    lateinit var imm: InputMethodManager

    var pageSize = 20
    var page = 1
    var searchText = ""
    var orderBy = "relevant"
    var itemSelected = 0
    var orientationSelected = 0
    var orientation: String? = null

    private val searchResultLiveData = MutableLiveData<String>()
    private val pageLiveData = MutableLiveData<Int>()

    val searchList = ArrayList<UnsplashPhoto>()

    val searchLiveData = Transformations.switchMap(searchResultLiveData) {
        Repository.searchPhotos(App.ACCESS_KEY, it, page, pageSize, orderBy, orientation)
    }

    val loadPageLiveData = Transformations.switchMap(pageLiveData) {
        Repository.searchPhotos(App.ACCESS_KEY, searchText, it, pageSize, orderBy, orientation)
    }

    fun searchPhotos(criteria: String) {
        page = 1
        searchResultLiveData.value = criteria
        searchText = criteria
    }

    fun loadPage(page: Int) {
        pageLiveData.value = page
    }


    fun actionDone(editText: EditText, recyclerView: RecyclerView) {
        Log.d(App.TAG, "searchByUrl: ")
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                orderBy = "relevant"
                orientation = null
                itemSelected = 0
                orientationSelected = 0
            //    recyclerView.smoothScrollToPosition(0)
                recyclerView.visibility = View.GONE
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
       // if (editText.text.isNullOrEmpty()) Tools.showSnackBar(editText, "Please Input")
    }

    private fun searchWithUrl(editText: EditText) {
        // swipeRefreshLayout.isRefreshing = true
        if (editText.text.isNullOrEmpty()) {
            Tools.showSnackBar(editText, "Please Input")
        } else {
            hideKeyboard(editText)
            searchPhotos(editText.text.toString())
        }

       // editText.setText("")
    }

    private fun hideKeyboard(editText: EditText) {
        imm = editText.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}
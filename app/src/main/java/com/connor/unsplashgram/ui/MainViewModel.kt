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

    lateinit var imm: InputMethodManager

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

    fun actionDone(editText: EditText, cardView: CardView) {
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchWithUrl(editText)
                cardView.visibility = View.GONE
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
          //  searchSauceNao(ptUrl.text.toString())

            editText.setText("")
    }

    private fun hideKeyboard(editText: EditText) {
        imm = editText.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }


}
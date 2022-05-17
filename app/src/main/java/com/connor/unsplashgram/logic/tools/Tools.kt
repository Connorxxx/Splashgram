package com.connor.unsplashgram.logic.tools

import android.view.View
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import com.google.android.material.snackbar.Snackbar

object Tools {

    fun showSnackBar(view: View, text: String) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
    }

    fun ImageView.loadWithQuality(
        highQuality: String,
        lowQuality: String,
        placeholderRes: Int? = null,
        errorRes: Int? = null
    ) {
        load(lowQuality) {
            placeholderRes?.let {
                placeholder(placeholderRes)
            }
            listener(onSuccess = { _, _ ->
                load(highQuality) {
                    placeholder(drawable) // If there was a way to not clear existing image before loading, this would not be required
                    errorRes?.let { error(errorRes) }
                }
            })
        }
    }
}
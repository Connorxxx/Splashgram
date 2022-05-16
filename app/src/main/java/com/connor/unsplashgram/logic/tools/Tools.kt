package com.connor.unsplashgram.logic.tools

import android.view.View
import com.google.android.material.snackbar.Snackbar

object Tools {

    fun showSnackBar(view: View, text: String) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
    }
}
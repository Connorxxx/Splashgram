package com.connor.unsplashgram.logic.tools

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import coil.load
import com.google.android.material.snackbar.Snackbar

object Tools {

    fun showSnackBar(view: View, text: String): Snackbar {
        val sbView =  Snackbar.make(view, text, Snackbar.LENGTH_LONG)
        sbView.show()
        return sbView
    }

    fun ImageView.loadWithQuality(
        highQuality: String,
        lowQuality: String,
        placeholderRes: Int? = null,
        errorRes: Int? = null,
        progressBar: ProgressBar? = null
    ) {
        load(lowQuality) {
            placeholderRes?.let {
                placeholder(placeholderRes)
            }
            listener(onSuccess = { request, result ->
                load(highQuality) {
                    placeholder(drawable) // If there was a way to not clear existing image before loading, this would not be required
                    errorRes?.let { error(errorRes) }
                }
            })
        }
    }

    fun openLink(link: String, context: Context, view: View) {
        val sourceUri = link.toUri()
        if (sourceUri.toString().startsWith("http")) {
            val openURI = sourceUri.toString()
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, openURI.toUri())
        } else {
            Snackbar.make(view, "this $link is not a URL", Snackbar.LENGTH_LONG).show()
        }
    }

    fun shareLink(source: String, context: Context, view: View) {
        if (source.startsWith("http")) {
            val sharedIntent = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, source)
                type = "text/*"
            }, null)
            context.startActivity(sharedIntent)
        } else {
            Snackbar.make(view, "this $source is not a URL", Snackbar.LENGTH_LONG).show()
        }
    }
}
package com.connor.unsplashgram.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.load
import com.connor.unsplashgram.R
import com.connor.unsplashgram.common.BaseActivity
import com.connor.unsplashgram.logic.tools.Tools.loadWithQuality
import kotlinx.android.synthetic.main.activity_photo_view.*

class PhotoViewActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val imgSource = getIntentString("image_regular")
        val imgFull = getIntentString("image_full")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)

        hideSystemUI()
        imgViewer.controller.settings.doubleTapZoom = 1f
        imgViewer.loadWithQuality(
            imgFull!!,
            imgSource!!,
            null,
            null
        )
        //imgViewer.load(imgSource)
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, imgViewer).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
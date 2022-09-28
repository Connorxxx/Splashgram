package com.connor.unsplashgram.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import com.alexvasilkov.gestures.views.GestureImageView
import com.connor.unsplashgram.App
import com.connor.unsplashgram.App.Companion.imgFull
import com.connor.unsplashgram.App.Companion.imgRaw
import com.connor.unsplashgram.App.Companion.imgSource
import com.connor.unsplashgram.R
import com.connor.unsplashgram.common.BaseActivity
import com.connor.unsplashgram.databinding.ActivityPhotoViewBinding
import com.connor.unsplashgram.logic.tools.Tools.loadWithQuality
import com.connor.unsplashgram.logic.tools.Tools.showSnackBar
import com.connor.unsplashgram.logic.tools.fileExists
import com.connor.unsplashgram.logic.tools.showFileExistsDialog
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.TipUtils.toast
import com.drake.net.utils.scopeNetLife
import java.io.File


class PhotoViewActivity : BaseActivity() {

    lateinit var manager: NotificationManager

    lateinit var imgViewer: GestureImageView
    lateinit var clPhotoZoom: ConstraintLayout
    lateinit var toolbar: Toolbar

    lateinit var file: File

   // lateinit var imgFull: String

    var fileName = "${App.userName}-${App.id}.jpg"

    //val path = "/storage/emulated/0/Pictures/Splashgram"

    override fun onCreate(savedInstanceState: Bundle?) {

        file = getAppSpecificAlbumStorageDir("Splashgram")

        super.onCreate(savedInstanceState)
        val binding: ActivityPhotoViewBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_photo_view)
        toolbar = binding.toolbarZoom
        setActionBarAndHome(toolbar)
       // window.statusBarColor = getColor(R.color.transparent)
        imgViewer = binding.imgViewer
        clPhotoZoom = binding.clPhotoZoom

        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

       //  hideSystemUI()
       // supportActionBar?.hide()
        imgViewer.controller.settings
            .setMaxZoom(3f)
            .setOverzoomFactor(4f)
            .doubleTapZoom = 0.97f
        imgViewer.loadWithQuality(
            imgFull,
            imgSource
        )

        Log.d(App.TAG, "onCreate: ${file.path}")

        //imgViewer.load(imgSource)


        binding.imgViewer.setOnClickListener {
          if (supportActionBar?.isShowing == false) showSystemUI() else hideSystemUI()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.item_test, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_set_as -> {
                if (fileExists(fileName)) {
                    setWallpaper("${file.path}/$fileName")
                } else downloadPhotos(imgRaw)
               val sbView = showSnackBar(imgViewer, "Please waiting...")
                sbView.setBackgroundTint(ContextCompat.getColor(this, R.color.white))
                sbView.setTextColor(ContextCompat.getColor(this, R.color.black))
            }

        }
        return super.onOptionsItemSelected(item)

    }

    private fun downloadPhotos(downloadUrl: String) {
        val downloadScope = scopeNetLife {
            val file = Get<File>(downloadUrl) {
                setDownloadFileName(fileName)
                setDownloadDir(file)
                addDownloadListener(object : ProgressListener() {
                    override fun onProgress(p: Progress) {
                        val channel = NotificationChannel(
                            "download",
                            "Download",
                            NotificationManager.IMPORTANCE_LOW
                        )
                        manager.createNotificationChannel(channel)

                        val notification =
                            NotificationCompat.Builder(
                                this@PhotoViewActivity, "download"
                            )
                                .setContentTitle("Photo size ${p.totalSize()}")
                                .setProgress(100, p.progress(), false)
                                .setOngoing(true)
                                .setSmallIcon(R.drawable.ic_baseline_get_app_24)
                                .build()
                        manager.notify(1, notification)
                    }

                })
            }.await()
            Log.d(App.TAG, "onOptionsItemSelected22: $file")
            manager.cancel(1)
            val fileName = file.path.toString()
            setWallpaper(fileName)
        }
    }

    private fun setWallpaper(file: String) {
            val intent = Intent(Intent.ACTION_ATTACH_DATA)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.setDataAndType(file.toUri(), "image/*")
            intent.putExtra("mimeType", "image/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "Set as:"))

    }


    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, toolbar).apply {
            hide(WindowInsetsCompat.Type.statusBars())
        }


        supportActionBar?.hide()
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, toolbar).apply {
            show(WindowInsetsCompat.Type.statusBars())


        }
        supportActionBar?.show()


//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        WindowInsetsControllerCompat(window, imgViewer).let { controller ->
//            controller.show(WindowInsetsCompat.Type.statusBars())
//            controller.show(WindowInsetsCompat.Type.navigationBars())
//            //controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
//        }
    }
}
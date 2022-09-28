package com.connor.unsplashgram.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.transform.CircleCropTransformation
import com.connor.unsplashgram.App
import com.connor.unsplashgram.App.Companion.id
import com.connor.unsplashgram.App.Companion.imgFull
import com.connor.unsplashgram.App.Companion.imgSource
import com.connor.unsplashgram.App.Companion.userName
import com.connor.unsplashgram.App.Companion.userProfile
import com.connor.unsplashgram.App.Companion.username
import com.connor.unsplashgram.R
import com.connor.unsplashgram.common.BaseActivity
import com.connor.unsplashgram.databinding.ActivityPhotoDetailBinding
import com.connor.unsplashgram.logic.tools.Tools.openLink
import com.connor.unsplashgram.logic.tools.Tools.shareLink
import com.connor.unsplashgram.logic.tools.Tools.showSnackBar
import com.connor.unsplashgram.logic.tools.fileExists
import com.connor.unsplashgram.logic.tools.showFileExistsDialog
import com.connor.unsplashgram.logic.tools.toPrettyString
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.scope.NetCoroutineScope
import com.drake.net.utils.scopeNetLife
import java.io.File

class PhotoDetailActivity : BaseActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this)[PhotoDetailViewModel::class.java]
    }

    private val TAG = "PhotoDetailActivity"

    lateinit var downloadScope: NetCoroutineScope

    lateinit var manager: NotificationManager
    lateinit var imgPhotoDetail: ImageView
    lateinit var tvName: TextView
    lateinit var imgUserProfile: ImageView
 //   lateinit var path: String
    lateinit var file: File

    companion object {
        var fileName = "$userName-$id.jpg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val binding: ActivityPhotoDetailBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_photo_detail)
        imgPhotoDetail = binding.imgPhotoDetail
        tvName = binding.tvName
        imgUserProfile = binding.imgUserProfile

        setActionBarAndHome(binding.toolbarDetail)
        window.statusBarColor = getColor(R.color.transparent) //colorStatusDark
         manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        viewModel.getPhotos(id)

        val file = getAppSpecificAlbumStorageDir("Splashgram")

//        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
//            val test = verticalOffset + binding.toolBarLayou.height - binding.toolbarDetail.height
//            Log.d(App.TAG, "onScrollChange: $verticalOffset  $test  ${binding.toolbarDetail.height}")
//            if (binding.toolbarDetail.height > test) {
//                binding.toolbarDetail.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
//            } else {
//                binding.toolbarDetail.setNavigationIcon(R.drawable.ic_detail_back)
//            }
//        })

        Log.d(TAG, "onCreate: ")

        imgPhotoDetail.load(imgSource)
        imgUserProfile.load(userProfile) {
            transformations(CircleCropTransformation())
        }
        tvName.text = userName
            //userName
        imgPhotoDetail.setOnClickListener {
            val intent = Intent(this, PhotoViewActivity::class.java).apply {
                putExtra("image_regular", imgSource)
                putExtra("image_full", imgFull)
            }
            startActivity(intent)
        }
        binding.vUserDetail.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java).apply {
               // putExtra("text_user_name", userName)
                putExtra("user_profile", userProfile)
                putExtra("username", username)
            }
            startActivity(intent)
        }

       // fileName = "$userName-$id.jpg"

        viewModel.getLiveData.observe(this) {
            val photo = it.getOrNull()
            if (photo != null) {
                binding.apply {
                    binding.photo = photo
                    photo.location?.let { location ->
                        if (photo.location.city != null || photo.location.country != null)
                            linearLocation.visibility = View.VISIBLE
                        val locationString = when {
                            location.city != null && location.country != null ->
                                getString(R.string.location, location.city, location.country)
                            location.city != null && location.country == null -> location.city
                            location.city == null && location.country != null -> location.country
                            else -> null
                        }
                        tvLocation.text = locationString
                    }
                    imgOpenHtml.setOnClickListener {
                        openLink(photo.links.html, this@PhotoDetailActivity, imgOpenHtml)
                    }
                    imgShare.setOnClickListener {
                        shareLink(photo.links.html, this@PhotoDetailActivity, imgShare)
                    }
                    photo.urls.raw?.let { raw ->
                        imgDownload.setOnClickListener {
                            if (fileExists(fileName)) {
                                showFileExistsDialog(this@PhotoDetailActivity) {
                                    downloadPhotos(
                                        raw,
                                        file.path.toString()
                                    )
                                }
                            } else {
                                downloadPhotos(raw, file.path.toString())
                            }
                        }
                    }
                }
            } else {
                App.ACCESS_KEY = "bLzTnLl8iuQLKaPweoWUUNJHe4VAYaIEIk4UIjFyB-E" //Enter another key to combat API limit
                showSnackBar(imgPhotoDetail, "the API reaches limit, Change KEY...")
            }
        }
    }

    private fun downloadPhotos(downloadUrl: String, path: String) {

        downloadScope = scopeNetLife {
            val file = Get<File>(downloadUrl) {
                setDownloadFileName(fileName)
                setDownloadDir(path)
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
                                this@PhotoDetailActivity, "download"
                            )
                                .setContentTitle("Photo size ${p.totalSize()}")
                                .setProgress(100, p.progress(), false)
                                .setOngoing(true)
                                .setSmallIcon(R.drawable.ic_baseline_get_app_24)
                                .build()
                        manager.notify(1, notification)
                    }

                })
                Log.d(TAG, "onCreate: download")
            }.await()

            Log.d(TAG, "downloadPhotos: ${file.path}")
            fun getViewPendingIntent(uri: Uri): PendingIntent {
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    setDataAndType(uri, "image/*")
                }
                val chooser = Intent.createChooser(viewIntent, "Open with")
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                return PendingIntent.getActivity(this@PhotoDetailActivity, 0, chooser, flags)
            }
            manager.cancel(1)
            val channel2 =
                NotificationChannel(
                    "done", "Done", NotificationManager.IMPORTANCE_HIGH
                )
            manager.createNotificationChannel(channel2)
            val notification = NotificationCompat.Builder(
                this@PhotoDetailActivity, "done"
            )
                .setContentTitle(fileName)
                .setContentText("Download Complete")
                .setSmallIcon(R.drawable.ic_baseline_file_download_done_24)
                .setContentIntent(getViewPendingIntent(file.path.toUri()))
                .setAutoCancel(true)
                .build()
            manager.notify(2, notification)
            /**刷新媒体库*/
            MediaScannerConnection.scanFile(
                this@PhotoDetailActivity, arrayOf(file.path.toString()),
                null, null
            )
        }
    }

    //    path = Environment.getExternalStoragePublicDirectory(
    //                Environment.DIRECTORY_PICTURES
    //            ).absolutePath + "/Splashgram"

    //path = Environment.getExternalStorageDirectory().absolutePath + "/Pictures/Splashgram"

}

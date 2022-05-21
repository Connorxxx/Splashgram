package com.connor.unsplashgram.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.transform.CircleCropTransformation
import com.connor.unsplashgram.App
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
    lateinit var fileName: String
    lateinit var imgPhotoDetail: ImageView
    lateinit var tvName: TextView
    lateinit var imgUserProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {

        val imgSource = getIntentString("image_regular")
        val imgFull = getIntentString("image_full")
        val userName = getIntentString("text_user_name")
        val userProfile = getIntentString("user_profile")
        val id = getIntentString("id")!!
        val username = getIntentString("username") ?: ""


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

        val path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ).absolutePath + "/Splashgram"
        val file = File(path)
        if (!file.exists()) file.mkdir()

        Log.d(TAG, "onCreate: ")

        imgPhotoDetail.load(imgSource)
        imgUserProfile.load(userProfile) {
            transformations(CircleCropTransformation())
        }
        tvName.text = userName
        imgPhotoDetail.setOnClickListener {
            val intent = Intent(this, PhotoViewActivity::class.java).apply {
                putExtra("image_regular", imgSource)
                putExtra("image_full", imgFull)
            }
            startActivity(intent)
        }
        binding.vUserDetail.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java).apply {
                putExtra("text_user_name", userName)
                putExtra("user_profile", userProfile)
                putExtra("username", username)
            }
            startActivity(intent)
        }

        fileName = "$userName-$id.jpg"

        viewModel.getLiveData.observe(this) {
            val photo = it.getOrNull()
            if (photo != null) {
                binding.apply {
                    tvView.text = photo.views?.toPrettyString()
                    tvDownload.text = photo.downloads?.toPrettyString()
                    tvLikes.text = photo.likes?.toPrettyString()
                    tvDate.text = photo.created_at.substring(0, 10)
                    tvDesc.text = photo.description ?: "The artist did not add a description..."
                    tvWH.text = getString(R.string.width_height, photo.width, photo.height)
                    photo.exif?.let { exif ->
                        if (exif.model != null)
                            linearCamera.visibility = View.VISIBLE
                        tvCamera.text = exif.model
                        tvCameraDetail.text = getString(
                            R.string.camera_detail,
                            exif.aperture,
                            exif.exposure_time,
                            exif.focal_length,
                            exif.iso
                        )
                    }
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
                            if (fileExists(fileName, path)) {
                                showFileExistsDialog(this@PhotoDetailActivity) { downloadPhotos(raw, path) }
                            } else {
                                downloadPhotos(raw, path)
                            }
                        }
                    }
                }
            } else {
                viewModel.getPhotos(id)
                showSnackBar(imgPhotoDetail, "Get details failure, Retry...")
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


}
//if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

//   String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myNewFolder";

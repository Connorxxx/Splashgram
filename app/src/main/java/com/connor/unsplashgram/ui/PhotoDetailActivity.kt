package com.connor.unsplashgram.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import coil.load
import coil.transform.CircleCropTransformation
import com.connor.unsplashgram.App
import com.connor.unsplashgram.R
import com.connor.unsplashgram.common.BaseActivity
import com.connor.unsplashgram.converter.GsonConverter
import com.connor.unsplashgram.converter.SerializationConverter
import com.connor.unsplashgram.databinding.ActivityPhotoDetailBinding
import com.connor.unsplashgram.logic.Repository
import com.connor.unsplashgram.logic.model.HomeBannerModel
import com.connor.unsplashgram.logic.model.UnsplashPhoto
import com.connor.unsplashgram.logic.tools.Tools
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.scope.NetCoroutineScope
import com.drake.net.utils.scopeNetLife
import java.io.File

class PhotoDetailActivity : BaseActivity() {

    private val TAG = "PhotoDetailActivity"

    lateinit var downloadScope: NetCoroutineScope
    lateinit var manager: NotificationManager
    lateinit var fileName: String
    lateinit var toolbarDetail: Toolbar
    lateinit var imgPhotoDetail: ImageView
    lateinit var tvName: TextView
    lateinit var imgUserProfile: ImageView
    lateinit var imgOpenHtml: ImageView
    lateinit var imgShare: ImageView
    lateinit var imgDownload: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {

        val imgSource = getIntentString("image_regular")
        val imgFull = getIntentString("image_full")
        val userName = getIntentString("text_user_name")
        val userProfile = getIntentString("user_profile")
        val downloadUrl = getIntentString("download_url")
        val id = getIntentString("id")
        val html = getIntentString("html")!!


        fileName = "$userName-$id.jpg"
        val photoPath = "photos/random/?client_id=${App.ACCESS_KEY}"

        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        super.onCreate(savedInstanceState)
        val binding: ActivityPhotoDetailBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_photo_detail)
        toolbarDetail = binding.toolbarDetail
        imgPhotoDetail = binding.imgPhotoDetail
        tvName = binding.tvName
        imgUserProfile = binding.imgUserProfile
        imgOpenHtml = binding.imgOpenHtml
        imgShare = binding.imgShare
        imgDownload = binding.imgDownload

        setActionBarAndHome(toolbarDetail)

        window.statusBarColor = getColor(R.color.transparent) //colorStatusDark

            scopeNetLife {
                val data = Get<List<UnsplashPhoto>>(photoPath) {
                    converter = GsonConverter() // 单例转换器, 此时会忽略全局转换器, 在Net中可以直接解析List等嵌套泛型数据
                }.await()
                Log.d(TAG, "onCreate: $photoPath scopeNetLife $data")
            }

        imgPhotoDetail.load(imgSource)
        tvName.text = userName
        imgUserProfile.load(userProfile) {
            transformations(CircleCropTransformation())
        }
        imgPhotoDetail.setOnClickListener {
            val intent = Intent(this, PhotoViewActivity::class.java).apply {
                putExtra("image_regular", imgSource)
                putExtra("image_full", imgFull)
            }
            startActivity(intent)
        }
        imgOpenHtml.setOnClickListener {
            Tools.openLink(html, this, imgOpenHtml)
        }
        imgShare.setOnClickListener {
            Tools.shareLink(html, this, imgShare)
        }
        val path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ).absolutePath + "/Splashgram"
        val file = File(path)
        if (!file.exists()) file.mkdir()

        imgDownload.setOnClickListener {
            Log.d(TAG, "onCreate: $fileName")
            downloadScope = scopeNetLife {
                val file = Get<File>(downloadUrl!!) {
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
                manager.cancel(1)
                //Tools.showSnackBar(imgDownload, "done")
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
                    .build()
                manager.notify(2, notification)
                Log.d(TAG, "onCreate: download done")

            }
        }
    }
}

//if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

//   String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myNewFolder";

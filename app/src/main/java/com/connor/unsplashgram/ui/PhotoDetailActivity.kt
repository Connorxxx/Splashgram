package com.connor.unsplashgram.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import coil.load
import coil.transform.CircleCropTransformation
import com.connor.unsplashgram.R
import com.connor.unsplashgram.common.BaseActivity
import com.connor.unsplashgram.databinding.ActivityPhotoDetailBinding
import com.connor.unsplashgram.logic.tools.Tools
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.scope.NetCoroutineScope
import com.drake.net.utils.scopeNetLife
import com.permissionx.guolindev.PermissionX
import kotlinx.android.synthetic.main.activity_photo_detail.*
import java.io.File
import java.util.*

class PhotoDetailActivity : BaseActivity() {

    private val TAG = "PhotoDetailActivity"

    private lateinit var downloadScope: NetCoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {

        val imgSource = getIntentString("image_regular")
        val imgFull = getIntentString("image_full")
        val userName = getIntentString("text_user_name")
        val userProfile = getIntentString("user_profile")
        val downloadUrl = getIntentString("download_url")
        val id = getIntentString("id")
        
        val fileName = "$userName-$id.jpg"

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        super.onCreate(savedInstanceState)
        val binding: ActivityPhotoDetailBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_photo_detail)

        setActionBarAndHome(toolbarDetail)

        window.statusBarColor = getColor(R.color.transparent) //colorStatusDark

        binding.imgPhotoDetail.load(imgSource)
        binding.tvName.text = userName
        binding.imgUserProfile.load(userProfile) {
            transformations(CircleCropTransformation())
        }
        binding.imgPhotoDetail.setOnClickListener {
            val intent = Intent(this, PhotoViewActivity::class.java).apply {
                putExtra("image_regular", imgSource)
                putExtra("image_full", imgFull)
            }
            startActivity(intent)
        }
        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Splashgram"
        val file = File(path)
        if (!file.exists()) file.mkdir()
        //this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        binding.imgDownload.setOnClickListener {
            Log.d(TAG, "onCreate: $fileName")
            downloadScope = scopeNetLife {
                val file = Get<File>(downloadUrl!!) {
                    setDownloadFileName(fileName)
                    setDownloadDir(path)
                    addDownloadListener(object : ProgressListener() {
                        override fun onProgress(p: Progress) {
                            val channel = NotificationChannel("download", "Download", NotificationManager.IMPORTANCE_DEFAULT)
                            manager.createNotificationChannel(channel)
                            val notification = NotificationCompat.Builder(this@PhotoDetailActivity, "test")
                                .setContentTitle("Photo size ${p.totalSize()}")
                                .setProgress(100, p.progress(), false)
                                .setSmallIcon(R.drawable.ic_download_border_24dp)
                                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_download_border_24dp))
                                .build()
                            manager.notify(1, notification)
                        }

                    })
                    Log.d(TAG, "onCreate: download")
                }.await()
                //Tools.showSnackBar(binding.imgDownload, "Done")
                manager.cancel(1)
                val channel = NotificationChannel("done", "Done", NotificationManager.IMPORTANCE_DEFAULT)
                manager.createNotificationChannel(channel)
                val notification = NotificationCompat.Builder(this@PhotoDetailActivity, "test")
                    .setContentTitle(fileName)
                    .setContentText("Download Complete")
                    .setSmallIcon(R.drawable.ic_download_border_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_download_border_24dp))
                    .build()
                manager.notify(2, notification)
            }
                Log.d(TAG, "onCreate: download done")
            }
        }
    }


        //if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

         //   String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myNewFolder";

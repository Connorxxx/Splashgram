package com.connor.unsplashgram.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import coil.load
import coil.transform.CircleCropTransformation
import com.connor.unsplashgram.R
import com.connor.unsplashgram.common.BaseActivity
import com.connor.unsplashgram.databinding.ActivityPhotoDetailBinding
import com.connor.unsplashgram.logic.tools.Tools
import com.drake.net.Get
import com.drake.net.scope.NetCoroutineScope
import com.drake.net.utils.scopeNetLife
import com.permissionx.guolindev.PermissionX
import kotlinx.android.synthetic.main.activity_photo_detail.*
import java.io.File
import java.util.*

class PhotoDetailActivity : BaseActivity() {

    private val TAG = "PhotoDetailActivity"

    private lateinit var downloadScope: NetCoroutineScope

    val randomString = UUID.randomUUID().toString().substring(0,12)

    override fun onCreate(savedInstanceState: Bundle?) {

        val imgSource = getIntentString("image_regular")
        val imgFull = getIntentString("image_full")
        val userName = getIntentString("text_user_name")
        val userProfile = getIntentString("user_profile")
        val downloadUrl = getIntentString("download_url")
        val id = getIntentString("id")
        
        val fileName = "$userName-$id.jpg"


        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_photo_detail)
        val binding: ActivityPhotoDetailBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_photo_detail)

        setSupportActionBar(toolbarDetail)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
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
                    //this@PhotoDetailActivity.filesDir
                    //storage/emulated/0/Pictures/Splashgram
                    Log.d(TAG, "onCreate: download")
                }.await()
                Tools.showSnackBar(binding.imgDownload, "Done")
                Log.d(TAG, "onCreate: download done")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
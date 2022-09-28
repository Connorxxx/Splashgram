package com.connor.unsplashgram.common


import android.annotation.SuppressLint
import android.content.Context
import android.os.*
import android.util.Log
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.connor.unsplashgram.App
import com.google.android.material.appbar.AppBarLayout
import java.io.File

open class BaseActivity() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun setActionBarAndHome(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun doubleToTop(
        toolbar: Toolbar,
        recyclerView: RecyclerView?,
        nestedScrollView: NestedScrollView?,
        appBarLayout: AppBarLayout?
    ) {
        val gestureDetector =
            GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    recyclerView?.smoothScrollToPosition(0)
                    nestedScrollView?.smoothScrollTo(0, 0)
                        appBarLayout?.setExpanded(true)
                    return super.onDoubleTap(e)
                }
            })
        toolbar.setOnTouchListener { v, p1 -> gestureDetector.onTouchEvent(p1) }
    }

    fun getAppSpecificAlbumStorageDir(albumName: String): File {
        lateinit var file: File
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            file = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ).absolutePath, albumName
            )
            if (!file.exists()) file.mkdir()
            Log.d(App.TAG, "testPath: ${file.path} >= Build.VERSION_CODES.R")
        } else {
            file = File(
                Environment.getExternalStorageDirectory().absolutePath + "/Pictures/",
                albumName
            )
            if (!file.exists()) file.mkdir()
            Log.d(App.TAG, "testPath: ${file.path}  !Build.VERSION_CODES.R")
        }
        return file
    }

   // fun getIntentString(string: String) = intent.getStringExtra(string)

}
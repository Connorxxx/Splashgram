package com.connor.unsplashgram.common


import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.kongqw.network.monitor.enums.NetworkState
import com.kongqw.network.monitor.util.NetworkStateUtils

open class BaseActivity() : AppCompatActivity() {

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
    fun doubleToTop(toolbar: Toolbar, recyclerView: RecyclerView) {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener(){
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                recyclerView.smoothScrollToPosition(0)
                return super.onDoubleTap(e)
            }
        })
        toolbar.setOnTouchListener { _, p1 -> gestureDetector.onTouchEvent(p1) }
    }

    fun getIntentString(string: String) = intent.getStringExtra(string)

}
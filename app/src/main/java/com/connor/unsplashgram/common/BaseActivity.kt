package com.connor.unsplashgram.common


import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
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

    fun getIntentString(string: String) = intent.getStringExtra(string)

}
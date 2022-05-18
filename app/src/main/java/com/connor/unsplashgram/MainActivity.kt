package com.connor.unsplashgram

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.connor.unsplashgram.databinding.ActivityMainBinding
import com.connor.unsplashgram.logic.tools.Tools
import com.connor.unsplashgram.ui.LoadAdapter
import com.connor.unsplashgram.ui.MainViewModel
import com.connor.unsplashgram.ui.SearchActivity

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val viewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    lateinit var adapter: LoadAdapter

    lateinit var toolbarMain: Toolbar
    lateinit var rvMain: RecyclerView
    lateinit var swipeRefresh: SwipeRefreshLayout


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        toolbarMain = binding.toolbarMain
        rvMain = binding.rvMain
        swipeRefresh = binding.swipeRefresh

        setSupportActionBar(toolbarMain)
        initRecyclerView()
        initViewModel()

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener(){
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                rvMain.smoothScrollToPosition(0)
                return super.onDoubleTap(e)
            }
        })
        toolbarMain.setOnTouchListener { _, p1 -> gestureDetector.onTouchEvent(p1) }
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        rvMain.layoutManager = layoutManager
        adapter = LoadAdapter(this, viewModel.loadList).apply {
            preloadItemCount = 4
            onPreload = {
                viewModel.loadPage(++viewModel.page)
            }
        }
        rvMain.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initViewModel() {
        swipeRefresh.setOnRefreshListener {
            viewModel.loadPhotos(App.ACCESS_KEY)
        }
        viewModel.loadPhotos(App.ACCESS_KEY)
        viewModel.loadLiveData.observe(this, Observer {
            val load = it.getOrNull()
            if (load != null) {
                viewModel.loadList.clear()
                viewModel.loadList.addAll(load)
                adapter.notifyDataSetChanged()
                swipeRefresh.isRefreshing = false
            } else {
                swipeRefresh.isRefreshing = false
                Tools.showSnackBar(rvMain, "NetWork ERROR")
                Log.d(App.TAG, "onCreate: ${it.isFailure}")
                it.exceptionOrNull()?.printStackTrace()
            }
        })
        viewModel.loadPageLiveData.observe(this, Observer {
            val load = it.getOrNull()
            if (load != null) {
                //viewModel.loadList.clear()
                viewModel.loadList.addAll(load)
                adapter.notifyDataSetChanged()
            } else {
                Tools.showSnackBar(rvMain, "NetWork ERROR")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.item_toobar, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
//                cardSearch.visibility = View.VISIBLE
//                edSearch.apply {
//                    viewModel.searchByUrl(this)
//                }
//                viewModel.actionDone(edSearch, cardSearch)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
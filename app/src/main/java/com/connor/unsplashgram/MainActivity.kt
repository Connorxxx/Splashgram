package com.connor.unsplashgram

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import coil.load
import com.connor.unsplashgram.logic.model.UnsplashPhoto
import com.connor.unsplashgram.logic.tools.Tools
import com.connor.unsplashgram.ui.LoadAdapter
import com.connor.unsplashgram.ui.MainViewModel
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.page
import com.drake.brv.utils.setup
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val viewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    lateinit var adapter: LoadAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //window.statusBarColor = getColor(R.color.colorStatusDark)
        setSupportActionBar(toolbar)
        toolbar.title = ""
        initRecyclerView()
        initViewModel()

        imgClear.setOnClickListener {
            cardSearch.visibility = View.GONE
        }
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        recyclerview.layoutManager = layoutManager
        adapter = LoadAdapter(this, viewModel.loadList).apply {
            preloadItemCount = 4
            onPreload = {
                viewModel.loadPage(++viewModel.page)
            }
        }
        recyclerview.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initViewModel() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadPhotos(App.ACCESS_KEY)
        }
        viewModel.loadPhotos(App.ACCESS_KEY)
        viewModel.loadLiveData.observe(this, Observer {
            val load = it.getOrNull()
            if (load != null) {
                viewModel.loadList.clear()
                viewModel.loadList.addAll(load)
                adapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            } else {
                swipeRefreshLayout.isRefreshing = false
                Tools.showSnackBar(recyclerview, "NetWork ERROR")
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
                Tools.showSnackBar(recyclerview, "NetWork ERROR")
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
                cardSearch.visibility = View.VISIBLE
                edSearch.apply {
                    viewModel.searchByUrl(this)
                }
                viewModel.actionDone(edSearch, cardSearch)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (cardSearch.visibility == View.VISIBLE || swipeRefreshLayout.isRefreshing) {
            cardSearch.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
        } else super.onBackPressed()
    }
}
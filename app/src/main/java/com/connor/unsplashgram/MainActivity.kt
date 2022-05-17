package com.connor.unsplashgram

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.connor.unsplashgram.logic.tools.Tools
import com.connor.unsplashgram.ui.LoadAdapter
import com.connor.unsplashgram.ui.MainViewModel
import com.connor.unsplashgram.ui.SearchActivity
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
        setSupportActionBar(toolbarSearch)
        toolbarSearch.title = ""
        initRecyclerView()
        initViewModel()
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
        srlViewer.setOnRefreshListener {
            viewModel.loadPhotos(App.ACCESS_KEY)
        }
        viewModel.loadPhotos(App.ACCESS_KEY)
        viewModel.loadLiveData.observe(this, Observer {
            val load = it.getOrNull()
            if (load != null) {
                viewModel.loadList.clear()
                viewModel.loadList.addAll(load)
                adapter.notifyDataSetChanged()
                srlViewer.isRefreshing = false
            } else {
                srlViewer.isRefreshing = false
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
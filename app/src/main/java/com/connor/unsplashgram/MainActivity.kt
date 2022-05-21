package com.connor.unsplashgram

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.connor.unsplashgram.common.BaseActivity
import com.connor.unsplashgram.databinding.ActivityMainBinding
import com.connor.unsplashgram.logic.tools.Tools.showSnackBar
import com.connor.unsplashgram.ui.LoadAdapter
import com.connor.unsplashgram.ui.MainViewModel
import com.connor.unsplashgram.ui.SearchActivity
import com.kongqw.network.monitor.NetworkMonitorManager
import com.kongqw.network.monitor.enums.NetworkState
import com.kongqw.network.monitor.interfaces.NetworkMonitor
import com.kongqw.network.monitor.util.NetworkStateUtils

class MainActivity : BaseActivity() {
    private val TAG = "MainActivity"

    private val viewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    lateinit var adapter: LoadAdapter

    lateinit var toolbarMain: Toolbar
    lateinit var rvMain: RecyclerView
    lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        toolbarMain = binding.toolbarMain
        rvMain = binding.rvMain
        swipeRefresh = binding.swipeRefresh
        NetworkMonitorManager.getInstance().register(this)

        setSupportActionBar(toolbarMain)
        initRecyclerView()
        initViewModel()

        doubleToTop(toolbarMain, rvMain)

        val networkState: NetworkState = NetworkStateUtils.getNetworkState(applicationContext)
        onNetWorkStateChange(networkState)
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkMonitorManager.getInstance().unregister(this)
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
            viewModel.loadList.clear()
            viewModel.loadPage(1)
        }
        viewModel.loadPage(viewModel.page)
        viewModel.loadPageLiveData.observe(this, Observer {
            val load = it.getOrNull()
            if (load != null) {
                //viewModel.loadList.clear()
                viewModel.loadList.addAll(load)
                adapter.notifyDataSetChanged()
                swipeRefresh.isRefreshing = false
            } else {
                //showSnackBar(rvMain, "Something went wrong...")

//                    swipeRefresh.isRefreshing = true
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

    @NetworkMonitor
    fun onNetWorkStateChange(networkState: NetworkState) {
        when (networkState) {
            NetworkState.NONE -> {
                //showSnackBar(rvMain, "No network connection")
            }
            NetworkState.WIFI -> {
                viewModel.loadPage(viewModel.page)
            }
            NetworkState.CELLULAR -> {
                viewModel.loadPage(viewModel.page)
            }
        }
    }
}
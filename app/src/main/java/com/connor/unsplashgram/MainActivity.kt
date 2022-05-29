package com.connor.unsplashgram

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
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
import java.io.File

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

        doubleToTop(toolbarMain, rvMain, null, null)

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
                Log.d(TAG, "initRecyclerView: ${viewModel.page}")
            }
        }
        rvMain.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initViewModel() {
        swipeRefresh.setOnRefreshListener {
            viewModel.loadList.clear()
            viewModel.page = 1
            viewModel.loadPage(viewModel.page)
        }
        viewModel.loadPage(viewModel.page)
        viewModel.loadPageLiveData.observe(this, Observer {
            val load = it.getOrNull()
            if (load != null) {
                //viewModel.loadList.clear()
                viewModel.loadList.addAll(load)
               // viewModel.loadList.removeAt(0)
                adapter.notifyItemChanged(load.lastIndex)
                // adapter.notifyDataSetChanged()
                swipeRefresh.isRefreshing = false
            } else {
                it.exceptionOrNull()?.printStackTrace()
                showSnackBar(rvMain, "the API reaches limit...")
                // viewModel.loadPage(viewModel.page)
            }
        })
        viewModel.orderLiveData.observe(this, Observer {
            val order = it.getOrNull()
            if (order != null) {
                Log.d(TAG, "initViewModel: ${viewModel.page}")
               // rvMain.scrollToPosition(0)
                viewModel.loadList.clear()
                viewModel.loadList.addAll(order)
                viewModel.loadList.removeAt(0)
                // adapter.notifyItemRangeChanged(0, order.size)
                Handler(Looper.getMainLooper()).postDelayed({
                    rvMain.scrollToPosition(0)
                    adapter.notifyDataSetChanged()
                    if (rvMain.visibility == View.GONE) rvMain.visibility = View.VISIBLE
                },80)

            } else {
                it.exceptionOrNull()?.printStackTrace()
                showSnackBar(rvMain, "the API reaches limit...")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.item_toobar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }
            R.id.filter -> {
                val singleChoiceItems = resources
                    .getStringArray(R.array.dialog_choice_array)
                val currentSelection = viewModel.itemSelected
                AlertDialog.Builder(this)
                    .setTitle(R.string.sort_by)
                    .setSingleChoiceItems(singleChoiceItems, currentSelection) { dialog, which ->
                        when (which) {
                            0 -> {
                                viewModel.itemSelected = 0
                                viewModel.orderBy = "latest"
                                Log.d(TAG, "onOptionsItemSelected: 0 $currentSelection")
                                if (which != currentSelection) {
                                    rvMain.visibility = View.GONE
                                    viewModel.orderPhotos(viewModel.orderBy)
                                }
                            }
                            1 -> {
                                viewModel.itemSelected = 1
                                viewModel.orderBy = "oldest"
                                Log.d(TAG, "onOptionsItemSelected: 1 $currentSelection")
                                if (which != currentSelection) {
                                    rvMain.visibility = View.GONE
                                    viewModel.orderPhotos(viewModel.orderBy)
                                }
                            }
                            2 -> {
                                viewModel.itemSelected = 2
                                viewModel.orderBy = "popular"
                                Log.d(TAG, "onOptionsItemSelected: 2 $currentSelection")
                                if (which != currentSelection) {
                                    rvMain.visibility = View.GONE
                                    viewModel.orderPhotos(viewModel.orderBy)
                                }
                            }

                        }
                        dialog.dismiss()
                    }
                    .show()
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
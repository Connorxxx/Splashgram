package com.connor.unsplashgram.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.connor.unsplashgram.R
import com.connor.unsplashgram.common.BaseActivity
import com.connor.unsplashgram.databinding.ActivitySearchBinding
import com.connor.unsplashgram.logic.tools.Tools

class SearchActivity : BaseActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(SearchViewModel::class.java)
    }

    lateinit var adapter: LoadAdapter
    lateinit var etSearch: EditText
    lateinit var srlSearch: SwipeRefreshLayout
    lateinit var rvSearch: RecyclerView
    lateinit var imgClean: ImageView
    lateinit var toolbarSearch: Toolbar

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySearchBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_search)
        etSearch = binding.etSearch
        srlSearch = binding.srlSearch
        rvSearch = binding.rvSearch
        imgClean = binding.imgClean
        toolbarSearch = binding.toolbarSearch

        setActionBarAndHome(toolbarSearch)

        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.searchByUrl(etSearch)
        }, 300)
        viewModel.actionDone(etSearch)
        srlSearch.isEnabled = false
        initRecyclerView()
        etSearch.addTextChangedListener {
            val content = it.toString()
            if (content.isNotEmpty())
                imgClean.visibility = View.VISIBLE
        }
        imgClean.setOnClickListener {
            etSearch.setText("")
            imgClean.visibility = View.GONE
        }
        viewModel.searchLiveData.observe(this, Observer {
            val result = it.getOrNull()
            if (result != null) {
                viewModel.searchList.clear()
                viewModel.searchList.addAll(result.results)
                adapter.notifyDataSetChanged()
                rvSearch.smoothScrollToPosition(0)
            } else {
                Tools.showSnackBar(rvSearch, "No photos here, Please check...")
            }
        })
        viewModel.loadPageLiveData.observe(this, Observer {
            val result = it.getOrNull()
            if (result != null) {
                //viewModel.loadList.clear()
                viewModel.searchList.addAll(result.results)
                adapter.notifyDataSetChanged()
            } else {
               // Tools.showSnackBar(rvSearch, "null")
            }
        })

    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        rvSearch.layoutManager = layoutManager
        adapter = LoadAdapter(this, viewModel.searchList).apply {
            preloadItemCount = 4
            onPreload = {
                viewModel.loadPage(++viewModel.page)
            }
        }
        rvSearch.adapter = adapter
    }
}
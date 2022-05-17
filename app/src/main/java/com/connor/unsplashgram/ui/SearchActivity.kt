package com.connor.unsplashgram.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.connor.unsplashgram.R
import com.connor.unsplashgram.logic.model.UnsplashPhoto
import com.connor.unsplashgram.logic.tools.Tools
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbarSearch
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(SearchViewModel::class.java)
    }

    lateinit var adapter: LoadAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(toolbarSearch)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.searchByUrl(etSearch)
        }, 300)
        viewModel.actionDone(etSearch)
        swipeRefreshLayout2.isEnabled = false
        initRecyclerView()
        etSearch.addTextChangedListener {
            val content = it.toString()
            if (content.isNotEmpty())
                imgClean.visibility = View.VISIBLE
        }
        imgClean.setOnClickListener {
            etSearch.setText("")
        }
        viewModel.searchLiveData.observe(this, Observer {
            val result = it.getOrNull()
            if (result != null) {
                viewModel.searchList.clear()
                viewModel.searchList.addAll(result.results)
                adapter.notifyDataSetChanged()
                recyclerview2.scrollToPosition(0)
            }
        })
        viewModel.loadPageLiveData.observe(this, Observer {
            val result = it.getOrNull()
            if (result != null) {
                //viewModel.loadList.clear()
                viewModel.searchList.addAll(result.results)
                adapter.notifyDataSetChanged()
            } else {
                Tools.showSnackBar(recyclerview, "NetWork ERROR")
            }
        })

    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        recyclerview2.layoutManager = layoutManager
        adapter = LoadAdapter(this, viewModel.searchList).apply {
            preloadItemCount = 4
            onPreload = {
                viewModel.loadPage(++viewModel.page)
            }
        }
        recyclerview2.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
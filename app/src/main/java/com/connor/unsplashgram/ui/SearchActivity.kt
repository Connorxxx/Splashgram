package com.connor.unsplashgram.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.connor.unsplashgram.App
import com.connor.unsplashgram.R
import com.connor.unsplashgram.common.BaseActivity
import com.connor.unsplashgram.databinding.ActivitySearchBinding
import com.connor.unsplashgram.databinding.DialogBottomSheetBinding
import com.connor.unsplashgram.logic.tools.Tools.showSnackBar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

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
    lateinit var eFab: ExtendedFloatingActionButton

    //@SuppressLint("NotifyDataSetChanged")
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
        eFab = binding.eFab

        setActionBarAndHome(toolbarSearch)

//        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.searchByUrl(etSearch)
//        }, 300)
        viewModel.actionDone(etSearch, rvSearch)
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
        eFab.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            val bindingSheet = DataBindingUtil.inflate<DialogBottomSheetBinding>(
                layoutInflater,
                R.layout.dialog_bottom_sheet,
                null,
                false
            )
            bottomSheetDialog.setContentView(bindingSheet.root)
            val currentSelection = viewModel.itemSelected
            val currentOrientationSelection = viewModel.orientationSelected
            when (currentSelection) {
                0 -> bindingSheet.rbRevelvant.isChecked = true
                1 -> bindingSheet.rbLatest.isChecked = true
            }
            when (currentOrientationSelection) {
                0 -> bindingSheet.rbAll.isChecked = true
                1 -> bindingSheet.rbLandscape.isChecked = true
                2 -> bindingSheet.rbPortrait.isChecked = true
                3 -> bindingSheet.rbSquarish.isChecked = true
            }
            bindingSheet.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rb_revelvant -> {
                        viewModel.orderBy = "relevant"
                        viewModel.itemSelected = 0
                    }
                    R.id.rb_latest -> {
                        viewModel.orderBy = "latest"
                        viewModel.itemSelected = 1
                    }
                }
            }
            bindingSheet.rgOrientation.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rb_all -> {
                        viewModel.orientation = null
                        viewModel.orientationSelected = 0
                    }
                    R.id.rb_landscape -> {
                        viewModel.orientation = "landscape"
                        viewModel.orientationSelected = 1
                    }
                    R.id.rb_portrait -> {
                        viewModel.orientation = "portrait"
                        viewModel.orientationSelected = 2
                    }
                    R.id.rb_squarish -> {
                        viewModel.orientation = "squarish"
                        viewModel.orientationSelected = 3
                    }
                }
            }
            bindingSheet.btnDialogBottomSheetOk.setOnClickListener {
                if (currentSelection != viewModel.itemSelected || currentOrientationSelection != viewModel.orientationSelected ) {
                    rvSearch.visibility = View.GONE
                    viewModel.searchPhotos(etSearch.text.toString())
                }
                bottomSheetDialog.dismiss()
            }
            bindingSheet.imgCancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.show()
        }


        viewModel.searchLiveData.observe(this, Observer {
            val result = it.getOrNull()
            if (result != null) {
                rvSearch.scrollToPosition(0)
                viewModel.searchList.clear()
                viewModel.searchList.addAll(result.results)
                adapter.notifyDataSetChanged()
               // adapter.notifyItemRangeChanged(0, result.results.size)

                if (eFab.visibility == View.GONE) eFab.visibility = View.VISIBLE
                if (rvSearch.visibility == View.GONE) rvSearch.visibility = View.VISIBLE
            } else {
                it.exceptionOrNull()?.printStackTrace()
                showSnackBar(rvSearch, "the API reaches limit...")
            }
        })
        viewModel.loadPageLiveData.observe(this, Observer {
            val result = it.getOrNull()
            if (result != null) {
                //viewModel.loadList.clear()
                viewModel.searchList.addAll(result.results)
                adapter.notifyItemChanged(result.results.lastIndex)
            } else {
               // it.exceptionOrNull()?.printStackTrace()
                showSnackBar(rvSearch, "the API reaches limit...")
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
        rvSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) eFab.shrink()
                else eFab.extend()
            }
        })
    }
}
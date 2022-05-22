package com.connor.unsplashgram.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.connor.unsplashgram.App
import com.connor.unsplashgram.R
import com.connor.unsplashgram.common.BaseActivity
import com.connor.unsplashgram.databinding.ActivityUserBinding
import com.connor.unsplashgram.logic.model.UnsplashPhoto
import com.connor.unsplashgram.logic.tools.Tools
import com.connor.unsplashgram.logic.tools.Tools.openLink
import com.connor.unsplashgram.logic.tools.Tools.shareLink
import com.connor.unsplashgram.logic.tools.Tools.showSnackBar
import com.connor.unsplashgram.logic.tools.toPrettyString
import com.drake.brv.utils.setup
import com.google.android.material.appbar.AppBarLayout
import okhttp3.internal.notifyAll

class UserActivity : BaseActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }

    lateinit var adapter: UserPhotosAdapter
    lateinit var rvUserPhotos: RecyclerView

    //@SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {

        val userName = getIntentString("text_user_name")
        val userProfile = getIntentString("user_profile")
        val username = getIntentString("username") ?: ""

        super.onCreate(savedInstanceState)
        val binding: ActivityUserBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_user)
        setActionBarAndHome(binding.toolbarUser)

        rvUserPhotos = binding.rvUserPhotos

        initRecyclerView()
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPage(viewModel.page)
        }
        doubleToTop(binding.toolbarUser, binding.rvUserPhotos, binding.nestedUserPhotos, binding.appbar)

        val isToolbarShown = false
        viewModel.getUserProfile(username)
        viewModel.getUserLiveData.observe(this) {
            val user = it.getOrNull()
            if (user != null) {
                binding.apply {
                    tvUsername.text = getString(R.string.username, user.username)
                    tvBio.text = user.bio ?: getString(R.string.default_bio, userName)
                    tvFollowing.text = user.following_count?.toPrettyString()
                    tvFollower.text = user.followers_count?.toPrettyString()
                    user.location?.let {
                        linearLocation.visibility = View.VISIBLE
                        tvUserLocation.text = user.location
                    }
                    user.portfolio_url?.let {
                        imgPortfolioUrl.visibility = View.VISIBLE
                        imgPortfolioUrl.setOnClickListener { img ->
                            openLink(user.portfolio_url, this@UserActivity, img)
                        }
                    }
                    imgUserHtml.setOnClickListener { img ->
                        openLink(user.links!!.html, this@UserActivity, img)
                    }
                    imgUserShare.setOnClickListener { img ->
                        shareLink(user.links!!.html, this@UserActivity, img)
                    }
                    appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                        val test = toolbarLayout.height / 2
//                        swipeRefresh.isEnabled = verticalOffset == 0
//                        if (verticalOffset + test < 0) {
//                            toolbarUser.title = userName
//                            toolbarUser.subtitle = getString(
//                                R.string.total_photos, user.total_photos?.toPrettyString()
//                            )
//                        } else {
//                            toolbarUser.title = " "
//                            toolbarUser.subtitle = " "
//                        }
                        Log.d(App.TAG, "onScrollChange: $verticalOffset  $test")
                    })
                    nestedUserPhotos.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY -> //if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()))
                        val test =  (v!!.getChildAt(0).getMeasuredHeight() - v!!.getMeasuredHeight())
                        //Log.d(App.TAG, "onScrollChange: $scrollY / $test / true / ")
                        if (scrollY == test && scrollY > oldScrollY) {
                         //   Log.d(App.TAG, "onScrollChange: $scrollY / $test / true / ")
                            viewModel.loadPage(++viewModel.page)
                        }
                        val shouldShowToolbar = scrollY > 0
                        //Log.d(App.TAG, "onScrollChange: $scrollY / $shouldShowToolbar / true / ")
                        if (shouldShowToolbar) {
                            toolbarUser.title = userName
                            toolbarUser.subtitle = getString(
                                R.string.total_photos, user.total_photos?.toPrettyString()
                            )
                        } else {
                            toolbarUser.title = " "
                            toolbarUser.subtitle = " "
                        }
                    })
                }
            } else {

            }
        }

        viewModel.loadPage(viewModel.page)

        viewModel.loadPageLiveData.observe(this) {
            val page = it.getOrNull()
            if (page != null) {
                viewModel.userPhotosList.addAll(page)
                //adapter.notifyDataSetChanged()
                adapter.notifyItemChanged(page.lastIndex)
                binding.swipeRefresh.isRefreshing = false
            } else {
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE
              //  showSnackBar(binding.userImageView, "None")
            }
        }

//        binding.page.onRefresh {
//            viewModel.loadPage(++viewModel.page)
//        }.autoRefresh()

        binding.apply {
            userImageView.load(userProfile) {
                transformations(CircleCropTransformation())
            }
            tvUserName.text = userName
        }
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        rvUserPhotos.layoutManager = layoutManager
        //rvUserPhotos.isNestedScrollingEnabled = false
        adapter = UserPhotosAdapter(this, viewModel.userPhotosList).apply {
//            preloadItemCount = 4
//            onPreload = {
//                viewModel.loadPage(++viewModel.page)
//            }
        }
        rvUserPhotos.adapter = adapter
    }
}
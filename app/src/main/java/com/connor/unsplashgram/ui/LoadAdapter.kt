package com.connor.unsplashgram.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import coil.load
import coil.transform.CircleCropTransformation
import com.connor.unsplashgram.App
import com.connor.unsplashgram.R
import com.connor.unsplashgram.databinding.ItemLoadBinding
import com.connor.unsplashgram.logic.model.UnsplashPhoto
import com.connor.unsplashgram.logic.tools.Tools.loadWithQuality
import com.drake.net.Get
import com.drake.net.utils.scopeNet
import java.lang.Integer.max

class LoadAdapter(private val ctx: Context, private val photosList: List<UnsplashPhoto>) :
    RecyclerView.Adapter<LoadAdapter.ViewHolder>() {

    // lateinit var userName: String

    // 预加载回调
    var onPreload: (() -> Unit)? = null

    // 预加载偏移量
    var preloadItemCount = 0

    // 列表滚动状态
    private var scrollState = SCROLL_STATE_IDLE

    inner class ViewHolder(private val binding: ItemLoadBinding)
        : RecyclerView.ViewHolder(binding.root) {
            fun getBinding(): ItemLoadBinding {
                return binding
            }
//        val imgLoad: ImageView = view.imgLoad
//        val tvLoad: TextView = view.tvLoad
//        val imgUser: ImageView = view.imgUser
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding: ItemLoadBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_load,
            parent,
            false
        )
        val holder = ViewHolder(binding)

        holder.getBinding().imgLoad.setOnClickListener {

            val imgSource = holder.getBinding().photo?.urls?.regular ?: ""
            val imgFull = holder.getBinding().photo?.urls?.full ?: ""
            val imgRaw = holder.getBinding().photo?.urls?.raw ?: ""
            val userName = holder.getBinding().photo?.user?.name ?: ""
            val userProfile = holder.getBinding().photo?.user?.profile_image?.large ?: ""
            val id = holder.getBinding().photo?.id ?: ""
            val username = holder.getBinding().photo?.user?.username ?: ""

            App.imgSource = imgSource
            App.imgFull = imgFull
            App.imgRaw = imgRaw
            App.userName = userName
            App.userProfile = userProfile
            App.id = id
            App.username = username

            val intent = Intent(ctx, PhotoDetailActivity::class.java).apply {
               // putExtra("image_regular", imgSource)
             //   putExtra("image_full", imgFull)
             //   putExtra("text_user_name", userName)
              //  putExtra("user_profile", userProfile)
              //  putExtra("id", id)
              //  putExtra("username", username)
            }
            ctx.startActivity(intent)
        }
        holder.getBinding().imgLoad.setOnLongClickListener {
            val imgSource = holder.getBinding().photo?.urls?.regular ?: ""
            val imgFull = holder.getBinding().photo?.urls?.full ?: ""
            val imgRaw = holder.getBinding().photo?.urls?.raw ?: ""
            val userName = holder.getBinding().photo?.user?.name ?: ""
            val id = holder.getBinding().photo?.id ?: ""

            App.imgSource = imgSource
            App.imgFull = imgFull
            App.imgRaw = imgRaw
            App.userName = userName
            App.id = id

            val intent = Intent(ctx, PhotoViewActivity::class.java).apply {
              //  putExtra("image_regular", imgSource)
             //   putExtra("image_full", imgFull)
            }
            ctx.startActivity(intent)
            return@setOnLongClickListener true
        }
        holder.getBinding().vUser.setOnClickListener {

            val userName = holder.getBinding().photo?.user?.name ?: ""
            val userProfile = holder.getBinding().photo?.user?.profile_image?.large ?: ""
            val username = holder.getBinding().photo?.user?.username ?: ""

            App.userName = userName
            App.userProfile = userProfile
            App.username = username


            val intent = Intent(ctx, UserActivity::class.java).apply {
             //   putExtra("text_user_name", userName)
             //   putExtra("user_profile", userProfile)
             //   putExtra("username", username)
            }
            ctx.startActivity(intent)
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        checkPreload(position)
        holder.getBinding().photo = photosList[position]
        val load = photosList[position]

        holder.getBinding().imgLoad.loadWithQuality(
            load.urls.regular!!,
            load.urls.thumb!!,
            R.drawable.loading,
            R.drawable.loading
        )

        holder.getBinding().imgUser.load(photosList[position].user.profile_image.large) {
            transformations(CircleCropTransformation())
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                // 更新滚动状态
                scrollState = newState
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    override fun getItemCount() = photosList.size

    // 判断是否进行预加载
    private fun checkPreload(position: Int) {
        if (onPreload != null
            && position == max(itemCount - 1 - preloadItemCount, 0)// 索引值等于阈值
            && scrollState != SCROLL_STATE_IDLE // 列表正在滚动
        ) {
            onPreload?.invoke()
        }
    }
}
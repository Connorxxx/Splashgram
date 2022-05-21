package com.connor.unsplashgram.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.connor.unsplashgram.App
import com.connor.unsplashgram.R
import com.connor.unsplashgram.databinding.ItemLoadBinding
import com.connor.unsplashgram.databinding.ItemUserPhotosBinding
import com.connor.unsplashgram.logic.model.UnsplashPhoto
import com.connor.unsplashgram.logic.tools.Tools.loadWithQuality

class UserPhotosAdapter(private val ctx: Context, private val photosList: List<UnsplashPhoto>) :
    RecyclerView.Adapter<UserPhotosAdapter.ViewHolder>() {

    // 预加载回调
    var onPreload: (() -> Unit)? = null

    // 预加载偏移量
    var preloadItemCount = 0

    // 列表滚动状态
    private var scrollState = RecyclerView.SCROLL_STATE_IDLE

    inner class ViewHolder(private val binding: ItemUserPhotosBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun getBinding(): ItemUserPhotosBinding {
            return binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemUserPhotosBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_user_photos,
            parent,
            false
        )
        val holder = ViewHolder(binding)
        holder.getBinding().imgLoad.setOnClickListener {
            val imgSource = holder.getBinding().photo?.urls?.regular
            val imgFull = holder.getBinding().photo?.urls?.full
            val userName = holder.getBinding().photo?.user?.name
            val userProfile = holder.getBinding().photo?.user?.profile_image?.large
            val id = holder.getBinding().photo?.id
            val username = holder.getBinding().photo?.user?.username

            val intent = Intent(ctx, PhotoDetailActivity::class.java).apply {
                putExtra("image_regular", imgSource)
                putExtra("image_full", imgFull)
                putExtra("text_user_name", userName)
                putExtra("user_profile", userProfile)
                putExtra("id", id)
                putExtra("username", username)
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
            && position == Integer.max(itemCount - 1 - preloadItemCount, 0)// 索引值等于阈值
            && scrollState != RecyclerView.SCROLL_STATE_IDLE // 列表正在滚动
        ) {
            onPreload?.invoke()
        }
    }
}
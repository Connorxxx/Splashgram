package com.connor.unsplashgram.ui

import android.content.Context
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
import com.connor.unsplashgram.R
import com.connor.unsplashgram.databinding.ItemLoadBinding
import com.connor.unsplashgram.logic.model.UnsplashPhoto
import java.lang.Integer.max

class LoadAdapter(private val ctx: Context, private val photosList: List<UnsplashPhoto>) :
    RecyclerView.Adapter<LoadAdapter.ViewHolder>() {

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
        return ViewHolder(binding)
//        val view = LayoutInflater.from(parent.context).inflate(
//            R.layout.item_load,
//            parent, false
//        )
//        return ViewHolder(view)
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

    private fun ImageView.loadWithQuality(
        highQuality: String,
        lowQuality: String,
        placeholderRes: Int? = null,
        errorRes: Int? = null
    ) {
        load(lowQuality) {
            placeholderRes?.let {
                placeholder(placeholderRes)
            }
            listener(onSuccess = { _, _ ->
                load(highQuality) {
                    placeholder(drawable) // If there was a way to not clear existing image before loading, this would not be required
                    errorRes?.let { error(errorRes) }
                }
            })
        }
    }
}
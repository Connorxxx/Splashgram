package com.connor.unsplashgram

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.drake.brv.BR
import com.drake.brv.utils.BRV
import com.drake.net.NetConfig
import com.kongqw.network.monitor.NetworkMonitorManager

class App : Application() {

    @SuppressLint("StaticFieldLeak")
    companion object {
        const val TAG = "UnsplashGram"
        const val ACCESS_KEY = "S02y3gYS3z7i-VnCy81gjAuyh5gMvewXxuqTPz7stfs"
        const val SECRET_KEY = "5hkDLR2dRkC6JPzrRPnsYjCqgYzSb6nMSWHDQqPL488"

        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        NetworkMonitorManager.getInstance().init(this)
        context = applicationContext
    }
}
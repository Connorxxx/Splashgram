package com.connor.unsplashgram

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.kongqw.network.monitor.NetworkMonitorManager

class App : Application() {

    var username = ""


    companion object {
        const val TAG = "UnsplashGram"
        var ACCESS_KEY = "FwwjrXyEstpJ_NPZCOggXf_SepXKCr_iDmOt5Z54LS8" //Input your own KEY
        const val SECRET_KEY = ""

        var userName = ""
        var imgSource = ""
        var imgFull = ""
        var imgRaw = ""
        var userProfile = ""
        var id = ""
        var username = ""

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        NetworkMonitorManager.getInstance().init(this)
        context = applicationContext
    }
}
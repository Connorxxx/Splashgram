package com.connor.unsplashgram

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.drake.net.NetConfig

class App : Application() {

    @SuppressLint("StaticFieldLeak")
    companion object {
        const val TAG = "UnsplashGram"
        const val ACCESS_KEY = "FwwjrXyEstpJ_NPZCOggXf_SepXKCr_iDmOt5Z54LS8"
        const val SECRET_KEY = "5hkDLR2dRkC6JPzrRPnsYjCqgYzSb6nMSWHDQqPL488"

        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}
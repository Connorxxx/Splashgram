package com.connor.unsplashgram.common

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    fun getIntentString(string: String) = intent.getStringExtra(string)

}
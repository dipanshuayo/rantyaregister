package com.bookies.register.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bookies.register.R
import kotlinx.android.synthetic.main.activity_about_our_set.*

class AboutOurSetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_our_set)
        val url:String="https://dipanshuayo.github.io/Aboutset2k21/about.html"
        about_set_web_view.loadUrl(url)
    }
}
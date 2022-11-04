package com.example.music.utils

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.music.R
import com.example.music.online.ui.activities.AccountActivity
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        //hide status bar
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

        //set color for status bar and navigation bar
        window.navigationBarColor = resources.getColor(R.color.nav_color, this.theme)
        window.statusBarColor = resources.getColor(R.color.nav_color, this.theme)

        if (getConnectionType(this) == ConnectionType.NOT_CONNECT){
            //switch to library
            showOfflineAlertDialog()
        }
        else {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, AccountActivity::class.java))
                finish()
            }, 100)
        }
    }
}
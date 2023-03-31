package com.example.music.utils

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.music.R
import com.example.music.offline.ui.activities.MainActivity
import com.example.music.online.ui.activities.LOGActivity
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

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (getConnectionType(this@SplashScreen) == ConnectionType.NOT_CONNECT){
                    AlertDialog
                        .Builder(this@SplashScreen)
                        .setMessage("Switch to offline mode?")
                        .setTitle("No internet connection")
                        .setPositiveButton("Yes") { _, _ ->
                            startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                        }
                        .setNegativeButton("Retry") { _, _ ->
                            handler.postDelayed(this, 1000)
                        }
                        .create()
                        .show()
                }
                else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this@SplashScreen, LOGActivity::class.java))
                        finish()
                    }, 1000)
                }
            }
        }, 1000)
    }
}
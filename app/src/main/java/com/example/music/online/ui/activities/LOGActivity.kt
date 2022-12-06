package com.example.music.online.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.music.R
import com.example.music.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LOGActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        toast("Please click BACK again to exit")

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false }
            ,2000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        setupActionBarWithNavController(navController)

        window.navigationBarColor = resources.getColor(R.color.main_color, this.theme)
        window.statusBarColor = resources.getColor(R.color.main_color, this.theme)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
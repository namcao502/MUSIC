package com.example.music.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.music.databinding.ActivityMainBinding
import com.example.music.databinding.ActivityOnlineMainBinding

class OnlineMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnlineMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}
package com.example.music.online.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.music.R
import com.example.music.databinding.ActivityCrudBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CRUDActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrudBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrudBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupActionBarWithNavController(findNavController(R.id.fragmentContainerView_crud))
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragmentContainerView_crud)
        return super.onSupportNavigateUp() || navController.navigateUp()
    }
}
package com.example.music.online.ui.activities

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.music.R
import com.example.music.databinding.ActivityCrudBinding
import com.example.music.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CRUDActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrudBinding

    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager())
                    toast("Permission accepted")
                else
                    toast("You denied the permission")
            } else {
                toast("You denied the permission")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrudBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupActionBarWithNavController(findNavController(R.id.fragmentContainerView_crud))

        if (checkPermission()) {
            toast("Permission accepted")
        } else {
            requestPermission() // Request Permission
        }

    }

    private val permissions = arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            AlertDialog.Builder(this@CRUDActivity)
                .setTitle("Permission")
                .setMessage("Please give the Storage permission")
                .setPositiveButton("Yes") { _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        intent.addCategory("android.intent.category.DEFAULT")
                        intent.data = Uri.parse(
                            String.format(
                                "package:%s",
                                *arrayOf<Any>(applicationContext.packageName)
                            )
                        )
                        activityResultLauncher.launch(intent)
                    } catch (e: Exception) {
                        val intent = Intent()
                        intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                        activityResultLauncher.launch(intent)
                    }
                }
                .setCancelable(false)
                .show()
        } else {
            ActivityCompat.requestPermissions(this@CRUDActivity, permissions, 30)
        }
    }


    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        }
        else {
            val readCheck = ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)
            val writeCheck = ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)
            readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragmentContainerView_crud)
        return super.onSupportNavigateUp() || navController.navigateUp()
    }
}
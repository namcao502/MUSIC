package com.example.music.utils

import android.app.Dialog
import android.app.ProgressDialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.music.R

fun Fragment.toast(message: String?){
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}

fun Fragment.createProgressDialog(title: String): ProgressDialog{
    val progressDialog = ProgressDialog(requireContext())
    progressDialog.setTitle(title)
    progressDialog.setMessage("Please wait...")
    progressDialog.setCancelable(false)
    return progressDialog
}

fun Fragment.createDialog(): Dialog{
    val dialog = Dialog(requireContext())
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)
    dialog.setContentView(R.layout.song_crud_dialog)

    //set size for dialog
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    lp.gravity = Gravity.CENTER
    dialog.window!!.attributes = lp

    return dialog
}
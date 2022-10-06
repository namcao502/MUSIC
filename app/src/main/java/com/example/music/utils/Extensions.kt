package com.example.music.utils

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.example.music.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

fun Fragment.toast(message: String?){
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}

fun Activity.toast(message: String?){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun changeEmailToXEmail(email: String): String {
    val splitEmail = email.split("@")
    var first = splitEmail[0]
    first = first.replaceRange(1, first.length, "x")
    return first + "@" + splitEmail[1]
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

fun Activity.createDialog(): Dialog{
    val dialog = Dialog(this)
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

fun Activity.createBottomSheetDialog(): BottomSheetDialog{
    val bottomSheetDialog = BottomSheetDialog(this)
    bottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    bottomSheetDialog.setCancelable(true)
    bottomSheetDialog.setContentView(R.layout.comment_dialog)

    //set size for dialog
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(bottomSheetDialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    lp.gravity = Gravity.CENTER
    bottomSheetDialog.window!!.attributes = lp

    return bottomSheetDialog
}
package com.example.music.utils

import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.example.music.R
import com.example.music.offline.ui.activities.MainActivity
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.ui.adapters.OnlineDialogPlaylistAdapter
import com.example.music.online.ui.fragments.DetailCollectionFragment
import com.example.music.online.viewModels.OnlinePlaylistViewModel
import com.example.music.online.viewModels.OnlineViewViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

fun View.fadeVisibility(visibility: Int, duration: Long = 400) {
    val transition: Transition = Fade()
    transition.duration = duration
    transition.addTarget(this)
    TransitionManager.beginDelayedTransition(this.parent as ViewGroup, transition)
    this.visibility = visibility
}

fun Activity.checkNetwork(){
    Handler(Looper.getMainLooper()).postDelayed({
        showOfflineAlertDialog()
    }, 1000)
}

fun Activity.showOfflineAlertDialog(){
    val builder = AlertDialog.Builder(this)

    builder.setMessage("Switch to offline mode?")
        .setTitle("No internet connection")
        .setPositiveButton("Yes") { _, _ ->
            startActivity(Intent(this, MainActivity::class.java))
        }
        .setNegativeButton("Retry") { _, _ ->
            this.recreate()
        }
    // Create the AlertDialog object and return it
    builder.create().show()
}

fun getConnectionType(context: Context): Int {
    var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi; 3: vpn
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        cm?.run {
            cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    result = 2
                } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    result = 1
                } else if (hasTransport(NetworkCapabilities.TRANSPORT_VPN)){
                    result = 3
                }
            }
        }
    } else {
        cm?.run {
            cm.activeNetworkInfo?.run {
                when (type) {
                    ConnectivityManager.TYPE_WIFI -> {
                        result = 2
                    }
                    ConnectivityManager.TYPE_MOBILE -> {
                        result = 1
                    }
                    ConnectivityManager.TYPE_VPN -> {
                        result = 3
                    }
                }
            }
        }
    }
    return result
}

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

fun Fragment.updateViewForModel(modelId: String, onlineViewViewModel: OnlineViewViewModel){
    onlineViewViewModel.updateView(modelId)
    onlineViewViewModel.updateView.observe(this){
        when(it) {
            is UiState.Loading -> {

            }
            is UiState.Failure -> {

            }
            is UiState.Success -> {
                Log.i("TAG502", "view updated for $modelId")
            }
        }
    }
}

fun downloadFile(context: Context,
                 fileName: String,
                 fileExtension: String,
                 destinationDirectory: String,
                 url: String){
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val uri: Uri = Uri.parse(url)
    val request = DownloadManager.Request(uri)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension)
    downloadManager.enqueue(request)
}

fun Fragment.createProgressDialog(title: String): ProgressDialog{
    val progressDialog = ProgressDialog(requireContext())
    progressDialog.setTitle(title)
    progressDialog.setMessage("Please wait...")
    progressDialog.setCancelable(false)
    return progressDialog
}

fun Fragment.createDialog(layout: Int): Dialog {

    val dialog = Dialog(requireContext())
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)
    dialog.setContentView(layout)

    //set size for dialog
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    lp.gravity = Gravity.CENTER
    dialog.window!!.attributes = lp

    return dialog
}

fun Activity.createDialog(layout: Int): Dialog {

    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)
    dialog.setContentView(layout)

    //set size for dialog
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    lp.gravity = Gravity.CENTER
    dialog.window!!.attributes = lp

    return dialog
}

fun Activity.createBottomSheetDialog(layout: Int): BottomSheetDialog {

    val bottomSheetDialog = BottomSheetDialog(this)
    bottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    bottomSheetDialog.setCancelable(true)
    bottomSheetDialog.setContentView(layout)

    //set size for dialog
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(bottomSheetDialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    lp.gravity = Gravity.CENTER
    bottomSheetDialog.window!!.attributes = lp

    return bottomSheetDialog
}

fun Fragment.createDialogForRenamePlaylist(playlist: OnlinePlaylist, onlinePlaylistViewModel: OnlinePlaylistViewModel){

    val builder = AlertDialog.Builder(requireContext())
    val inflater = this.layoutInflater
    val view = inflater.inflate(R.layout.menu_playlist_dialog, null)

    view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).setText(playlist.name)

    builder.setMessage("Rename")
        .setTitle("")
        .setView(view)
        .setPositiveButton("Rename") { _, _ ->

            val title = view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Name can not be empty", Toast.LENGTH_SHORT).show()
            } else {
                playlist.name = title
                FirebaseAuth.getInstance().currentUser?.let {
                    onlinePlaylistViewModel.updatePlaylistForUser(playlist, it)
                }
                onlinePlaylistViewModel.updatePlaylist.observe(this) {
                    when (it) {
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
                            Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        .setNegativeButton("Cancel") { _, _ ->
            // User cancelled the dialog
        }
    // Create the AlertDialog object and return it
    builder.create().show()
}

fun Fragment.createDialogForDeletePlaylist(playlist: OnlinePlaylist, onlinePlaylistViewModel: OnlinePlaylistViewModel){

    val builder = AlertDialog.Builder(requireContext())

    builder.setMessage("Delete ${playlist.name} playlist?")
        .setTitle("")
        .setPositiveButton("Delete") { _, _ ->

            FirebaseAuth.getInstance().currentUser?.let {
                onlinePlaylistViewModel.deletePlaylistForUser(playlist, it)
            }
            onlinePlaylistViewModel.deletePlaylist.observe(this) {
                when (it) {
                    is UiState.Loading -> {

                    }
                    is UiState.Failure -> {

                    }
                    is UiState.Success -> {
                        Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        .setNegativeButton("Cancel") { _, _ ->
            // User cancelled the dialog
        }
    // Create the AlertDialog object and return it
    builder.create().show()
}

fun Fragment.createDialogForAddPlaylist(onlinePlaylistViewModel: OnlinePlaylistViewModel){

    val builder = AlertDialog.Builder(requireContext())
    val inflater = layoutInflater
    val view = inflater.inflate(R.layout.menu_playlist_dialog, null)

    builder.setMessage("Create")
        .setTitle("")
        .setView(view)
        .setPositiveButton("Create") { _, _ ->

            val title =
                view.findViewById<EditText>(R.id.title_et_menu_playlist_dialog).text.toString()

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Name can not be empty", Toast.LENGTH_SHORT).show()
            } else {
                val playlist = OnlinePlaylist("", title, emptyList())
                FirebaseAuth.getInstance().currentUser?.let {
                    onlinePlaylistViewModel.addPlaylistForUser(playlist, it)
                }
                onlinePlaylistViewModel.addPlaylist.observe(this) {
                    when (it) {
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
                            Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        .setNegativeButton("Cancel") { _, _ ->
            // User cancelled the dialog
        }
    // Create the AlertDialog object and return it
    builder.create().show()
}

fun Fragment.createDialogForAddToPlaylist(onlinePlaylistViewModel: OnlinePlaylistViewModel, onlineDialogPlaylistAdapter: OnlineDialogPlaylistAdapter) {
    val dialog = createDialog(R.layout.fragment_online_playlist)

    val recyclerView = dialog.findViewById<RecyclerView>(R.id.playlist_recyclerView)
    recyclerView.adapter = onlineDialogPlaylistAdapter
    recyclerView.layoutManager = LinearLayoutManager(dialog.context)

    FirebaseAuth.getInstance().currentUser?.let {
        onlinePlaylistViewModel.getAllPlaylistOfUser(it)
    }
    onlinePlaylistViewModel.playlist.observe(viewLifecycleOwner){
        when(it){
            is UiState.Loading -> {

            }
            is UiState.Failure -> {

            }
            is UiState.Success -> {
                onlineDialogPlaylistAdapter.setData(it.data)
            }
        }
    }

    val addBtn = dialog.findViewById<FloatingActionButton>(R.id.add_btn)

    addBtn.setOnClickListener {
        createDialogForAddPlaylist(onlinePlaylistViewModel)
    }
    dialog.show()
}

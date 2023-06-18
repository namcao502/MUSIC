package com.example.music.online.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.FragmentUserBinding
import com.example.music.offline.ui.activities.MainActivity
import com.example.music.online.data.models.OnlineAccount
import com.example.music.online.ui.activities.LOGActivity
import com.example.music.online.ui.activities.OnlineMainActivity
import com.example.music.online.viewModels.FirebaseAuthViewModel
import com.example.music.online.viewModels.FirebaseViewModel
import com.example.music.online.viewModels.OnlineAccountViewModel
import com.example.music.utils.ConnectionType
import com.example.music.utils.UiState
import com.example.music.utils.createDialog
import com.example.music.utils.createProgressDialog
import com.example.music.utils.getConnectionType
import com.example.music.utils.toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.lang.String


@AndroidEntryPoint
class UserFragment: Fragment() {

    private var _binding: FragmentUserBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val accountViewModel: OnlineAccountViewModel by viewModels()
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private val firebaseAuthViewModel: FirebaseAuthViewModel by activityViewModels()

    private var isEditing = false

    private lateinit var currentAccount: OnlineAccount

    private var imgUri: Uri? = null

    // declare the GoogleSignInClient
    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signOutBtn.setOnClickListener {
            if (getConnectionType(requireContext()) == ConnectionType.NOT_CONNECT){
                AlertDialog
                    .Builder(requireContext(), R.style.AlertDialogTheme)
                    .setMessage("Switch to offline mode?")
                    .setTitle("No internet connection")
                    .setPositiveButton("Yes") { _, _ ->
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                    }
                    .setNegativeButton("Retry") { _, _ ->
                        binding.signOutBtn.performClick()
                    }
                    .create()
                    .show()
            }
            else {
                // call requestIdToken as follows
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

                firebaseAuthViewModel.signOut()

                (activity as OnlineMainActivity).stopService()
                (activity as OnlineMainActivity).handler.removeMessages(0)
                (activity as OnlineMainActivity).handler2.removeMessages(0)
                (activity as OnlineMainActivity).finish()

                mGoogleSignInClient.signOut()
                    .addOnSuccessListener {
                        startActivity(Intent(requireContext(), LOGActivity::class.java))
                    }
                    .addOnFailureListener {
                        startActivity(Intent(requireContext(), LOGActivity::class.java))
                    }
            }
        }

        binding.libraryBtn.setOnClickListener {
            (activity as OnlineMainActivity).stopService()
            (activity as OnlineMainActivity).handler.removeMessages(0)
            (activity as OnlineMainActivity).handler2.removeMessages(0)
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        val currentUserID = Firebase.auth.currentUser?.uid
        if (currentUserID != null){
            accountViewModel.getAccountByID(currentUserID)
            accountViewModel.accountByID.observe(viewLifecycleOwner){
                when(it){
                    is UiState.Loading -> {

                    }
                    is UiState.Failure -> {
                        toast(it.toString())
                    }
                    is UiState.Success -> {
                        currentAccount = it.data
                        binding.nameEt.setText(currentAccount.name)
                        binding.emailEt.setText(currentAccount.email)
                        binding.passwordEt.setText(currentAccount.password)
                        if (currentAccount.imgFilePath!!.isNotEmpty()){
                            Glide.with(requireContext()).load(currentAccount.imgFilePath).into(binding.userImg)
                        }
                        else {
                            binding.userImg.setImageResource(R.drawable.ic_baseline_people_24)
                        }
//                        if (currentAccount.role == FireStoreCollection.ADMIN){
//                            binding.manageBtn.visibility = View.VISIBLE
//                        }
                    }
                }
            }
        }

        binding.manageBtn.setOnClickListener {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (getConnectionType(requireContext()) == ConnectionType.NOT_CONNECT){
                        AlertDialog
                            .Builder(requireContext(), R.style.AlertDialogTheme)
                            .setMessage("Switch to offline mode?")
                            .setTitle("No internet connection")
                            .setPositiveButton("Yes") { _, _ ->
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                            }
                            .setNegativeButton("Retry") { _, _ ->
                                handler.postDelayed(this, 100)
                            }
                            .create()
                            .show()
                    }
                    else {
//                        startActivity(Intent(requireContext(), CRUDActivity::class.java))
                        (activity as OnlineMainActivity).stopService()
                    }
                }
            }, 500)

        }

        binding.editBtn.setOnClickListener {

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (getConnectionType(requireContext()) == ConnectionType.NOT_CONNECT){
                        AlertDialog
                            .Builder(requireContext(), R.style.AlertDialogTheme)
                            .setMessage("Switch to offline mode?")
                            .setTitle("No internet connection")
                            .setPositiveButton("Yes") { _, _ ->
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                            }
                            .setNegativeButton("Retry") { _, _ ->
                                handler.postDelayed(this, 100)
                            }
                            .create()
                            .show()
                    }
                    else {
                        isEditing = if (isEditing){
                            showUI(false)
                            false
                        } else {
                            showUI(true)
                            true
                        }
                    }
                }
            }, 500)

        }

        binding.userImg.setOnClickListener {
            if (isEditing){
                imageChooser()
            }
        }

        binding.userImg.setOnLongClickListener {
            if (currentAccount.imgFilePath!!.isNotEmpty()){
                val imgRef = FirebaseStorage
                    .getInstance()
                    .getReferenceFromUrl(currentAccount.imgFilePath.toString())
                imgRef.delete()
                    .addOnSuccessListener {
                        toast("Deleted ${currentAccount.name} old image")
                        currentAccount.imgFilePath = ""
                        updateAccount(currentAccount)
                    }
                    .addOnFailureListener {
                        toast("$it")
                    }
            }
            binding.userImg.setImageResource(R.drawable.ic_baseline_people_24)
            return@setOnLongClickListener true
        }

        binding.saveBtn.setOnClickListener {

            val dialog = createProgressDialog("Updating an account")

            val name = binding.nameEt.text.toString()
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()

            if (name.isEmpty()){
                toast("Please type a name...")
                return@setOnClickListener
            }

            if (email.isEmpty()){
                toast("Please type an email...")
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6){
                toast("Please type a password(at least 6 characters)...")
                return@setOnClickListener
            }

            if (name != currentAccount.name){
                dialog.show()
                currentAccount.name = name
                updateAccount(currentAccount)
                dialog.cancel()
            }

            if (imgUri != null){
                val progressDialog = createProgressDialog("Updating ${currentAccount.email}'s image...")
                firebaseViewModel.uploadSingleImageFile("Account Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            currentAccount.imgFilePath = it.data.toString()
                            updateAccount(currentAccount)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }

                if (currentAccount.imgFilePath!!.isNotEmpty()){
                    val imgRef = FirebaseStorage
                        .getInstance()
                        .getReferenceFromUrl(currentAccount.imgFilePath.toString())
                    imgRef.delete()
                        .addOnSuccessListener {
                            toast("Deleted old image")
                        }
                        .addOnFailureListener {
                            toast("$it")
                        }
                }
            }

            if (email != currentAccount.email){

                viewLifecycleOwner.lifecycleScope.launch {

                    dialog.show()
                    val user = Firebase.auth.currentUser
                    user!!.updateEmail(email)
                        .addOnSuccessListener {
                            currentAccount.email = email
                            updateAccount(currentAccount)
                            dialog.cancel()
                        }
                        .addOnFailureListener {
                            toast(it.toString())
                            dialog.cancel()
                        }

                }

            }

            if (password != currentAccount.password){
                viewLifecycleOwner.lifecycleScope.launch {
                    dialog.show()
                    val user = Firebase.auth.currentUser
                    user!!.updatePassword(password)
                        .addOnSuccessListener {
                            currentAccount.password = password
                            updateAccount(currentAccount)
                            dialog.cancel()
                        }
                        .addOnFailureListener {
                            toast(it.toString())
                            dialog.cancel()
                        }
                }

            }

            showUI(false)
            isEditing = false
        }

        binding.themeBtn.setOnClickListener {

            val dialog = createDialog(R.layout.theme_dialog)

            val beginTxt = dialog.findViewById<TextView>(R.id.preview_color_begin)
            val endTxt = dialog.findViewById<TextView>(R.id.preview_color_end)
            val previewTxt = dialog.findViewById<TextView>(R.id.preview_color)
            val saveBtn = dialog.findViewById<Button>(R.id.save_btn)
            val cancelBtn =  dialog.findViewById<Button>(R.id.cancel_btn)
            val defaultBtn =  dialog.findViewById<Button>(R.id.default_btn)

            var beginColor = "#aaaaaa"
            var endColor = "#aaaaaa"

            beginTxt.setOnClickListener {
                ColorPickerDialog.Builder(context, R.style.AlertDialogTheme)
                    .setTitle("Pick your color")
                    .setPositiveButton("Confirm", ColorEnvelopeListener { envelope, _ ->
                        beginColor = String.format("#%06X", 0xFFFFFF and envelope.color)
                        beginTxt.setBackgroundColor(Color.parseColor(beginColor))
                        beginTxt.text = "Picked"
                        val gd = GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor(beginColor), Color.parseColor(endColor))
                        )
                        previewTxt.setBackgroundDrawable(gd)
                    })
                    .setNegativeButton("Cancel") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .show()
            }

            endTxt.setOnClickListener {
                ColorPickerDialog.Builder(context, R.style.AlertDialogTheme)
                    .setTitle("Pick your color")
                    .setPositiveButton("Confirm", ColorEnvelopeListener { envelope, _ ->
                        endColor = String.format("#%06X", 0xFFFFFF and envelope.color)
                        endTxt.setBackgroundColor(Color.parseColor(endColor))
                        endTxt.text = "Picked"
                        val gd = GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor(beginColor), Color.parseColor(endColor))
                        )
                        previewTxt.setBackgroundDrawable(gd)
                    })
                    .setNegativeButton("Cancel") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .show()
            }

            defaultBtn.setOnClickListener {
                (activity as OnlineMainActivity).setStatusColor(false)
                (activity as OnlineMainActivity).setThemeColor("#A4508B", "#5F0A87")
                toast("Back to default")
                dialog.dismiss()
            }

            saveBtn.setOnClickListener {
                if (beginTxt.text.equals("Picked") && endTxt.text.equals("Picked")){
                    (activity as OnlineMainActivity).setThemeColor(beginColor, endColor)
                    toast("Please enjoy your new look")
                    dialog.dismiss()
                }
                else {
                    toast("Please pick both begin and end color")
                    return@setOnClickListener
                }
            }

            cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

        }
    }

    private fun showUI(check: Boolean){
        if (check){
            binding.editBtn.setImageResource(R.drawable.ic_baseline_cancel_24)
            binding.saveBtn.visibility = View.VISIBLE
            binding.textInputLayoutPW.visibility = View.VISIBLE
            binding.textInputLayout4.isEnabled = true
            binding.textInputLayout5.isEnabled = true
        }
        else {
            binding.editBtn.setImageResource(R.drawable.ic_baseline_edit_note_24)
            binding.saveBtn.visibility = View.GONE
            binding.textInputLayoutPW.visibility = View.GONE
            binding.textInputLayout4.isEnabled = false
            binding.textInputLayout5.isEnabled = false
        }
    }

    private fun updateAccount(account: OnlineAccount){
        accountViewModel.updateAccount(account)
        accountViewModel.updateAccount.observe(viewLifecycleOwner){
            when (it) {
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    toast(it.data)
                }
            }
        }
    }

    private fun imageChooser() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        launchSomeActivity.launch(i)
    }

    private var launchSomeActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            // do your operation from here....
            if (data != null && data.data != null) {
                try {
                    imgUri = data.data
                    binding.userImg.setImageURI(imgUri)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
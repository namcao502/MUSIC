package com.example.music.ui.fragments.online

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.UiState
import com.example.music.data.models.online.OnlineAccount
import com.example.music.databinding.FragmentAccountCrudBinding
import com.example.music.databinding.FragmentUserBinding
import com.example.music.utils.createProgressDialog
import com.example.music.utils.toast
import com.example.music.viewModels.online.FirebaseViewModel
import com.example.music.viewModels.online.OnlineAccountViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

@AndroidEntryPoint
class UserFragment: Fragment() {

    private var _binding: FragmentUserBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val accountViewModel: OnlineAccountViewModel by viewModels()

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private var isEditing = false

    private lateinit var currentAccount: OnlineAccount

    private var imgUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserID = Firebase.auth.currentUser!!.uid
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
                }
            }
        }

        binding.editBtn.setOnClickListener {
            if (isEditing){
                showUI(false)
                isEditing = false
            }
            else {
                showUI(true)
                isEditing = true
            }
        }

        binding.userImg.setOnClickListener {
            val intent = Intent()
            intent.type = "Song Images/"
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
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

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            try {
                imgUri = result.data?.data
                binding.userImg.setImageURI(imgUri)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
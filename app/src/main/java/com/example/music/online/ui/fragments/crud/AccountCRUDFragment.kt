package com.example.music.online.ui.fragments.crud

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.utils.UiState
import com.example.music.online.data.models.OnlineAccount
import com.example.music.databinding.FragmentAccountCrudBinding
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.utils.createProgressDialog
import com.example.music.utils.toast
import com.example.music.online.viewModels.FirebaseViewModel
import com.example.music.online.viewModels.OnlineAccountViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class AccountCRUDFragment : Fragment() {

    private var _binding: FragmentAccountCrudBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onlineAccountViewModel: OnlineAccountViewModel by viewModels()

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private var imgUri: Uri? = null

    private var currentAccount: OnlineAccount? = null

    private var adminAccount: OnlineAccount? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountCrudBinding.inflate(layoutInflater, container, false)

        onlineAccountViewModel.getAccountByID(Firebase.auth.currentUser!!.uid)
        onlineAccountViewModel.accountByID.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    adminAccount = it.data
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var accounts: List<OnlineAccount> = emptyList()

        onlineAccountViewModel.getAllAccounts()
        onlineAccountViewModel.account.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    accounts = it.data
                    with(binding.listView){
                        adapter = ArrayAdapter(requireContext(),
                            androidx.appcompat.R.layout.
                            support_simple_spinner_dropdown_item, accounts)
                    }
                }
            }
        }

        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(text: String): Boolean {
                //creating a new array list to filter our data.
                val filter: ArrayList<OnlineAccount> = ArrayList<OnlineAccount>()

                // running a for loop to compare elements.
                for (item in accounts) {
                    // checking if the entered string matched with any item of our recycler view.
                    if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                        // if the item is matched we are
                        // adding it to our filtered list.
                        filter.add(item)
                    }
                }
                if (filter.isEmpty() || text.isEmpty()) {
                    // if no item is added in filtered list we are
                    // displaying a toast message as no data found.
                    toast("Not found")
                    with(binding.listView){
                        adapter = ArrayAdapter(requireContext(),
                            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, accounts)
                    }
                } else {
                    // at last we are passing that filtered
                    // list to our adapter class.
                    with(binding.listView){
                        adapter = ArrayAdapter(requireContext(),
                            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, filter)
                    }
                }
                return false
            }

        })

        binding.resetBtn.setOnClickListener {
            binding.nameEt.setText("")
            binding.emailEt.setText("")
            binding.passwordEt.setText("")
            binding.roleEt.setText("")
            imgUri = null
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
        }

        binding.imgFile.setOnLongClickListener {
            if (currentAccount!!.imgFilePath!!.isNotEmpty()){
                val imgRef = FirebaseStorage
                    .getInstance()
                    .getReferenceFromUrl(currentAccount!!.imgFilePath.toString())
                imgRef.delete()
                    .addOnSuccessListener {
                        toast("Deleted ${currentAccount!!.name} old image")
                        currentAccount!!.imgFilePath = ""
                        updateAccount(currentAccount!!)
                    }
                    .addOnFailureListener {
                        toast("$it")
                    }
            }
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
            return@setOnLongClickListener true
        }

        binding.listView.setOnItemClickListener { _, _, i, _ ->

            currentAccount = accounts[i]
            binding.nameEt.setText(currentAccount!!.name)
            binding.emailEt.setText(currentAccount!!.email)
            binding.passwordEt.setText(currentAccount!!.password)
            binding.roleEt.setText(currentAccount!!.role)

            if (currentAccount!!.imgFilePath!!.isNotEmpty()){
                Glide.with(requireContext()).load(currentAccount!!.imgFilePath).into(binding.imgFile)
            }
            else {
                binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
            }

        }

        binding.imgFile.setOnClickListener {
            val intent = Intent()
            intent.type = "Song Images/"
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
        }

        binding.addBtn.setOnClickListener {

            val name = binding.nameEt.text.toString()
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()
            val role = binding.roleEt.text.toString()

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

            val dialog = createProgressDialog("Adding new account")
            dialog.show()
            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val account = OnlineAccount("", Firebase.auth.currentUser!!.uid, name, email, password, role, "")
                    if (imgUri != null){
                        val progressDialog = createProgressDialog("Adding an account's image...")
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
                                    account.imgFilePath = it.data.toString()
                                    addAccount(account)
                                    imgUri = null
                                    progressDialog.cancel()
                                }
                            }
                        }
                    }
                    else {
                        addAccount(account)
                    }
                    dialog.cancel()
                }
                .addOnFailureListener {
                    toast("Can not create")
                    dialog.cancel()
                }
        }

        binding.deleteBtn.setOnClickListener {

            if (currentAccount == null){
                toast("Please pick an account to delete...")
                return@setOnClickListener
            }

            val auth = Firebase.auth

            if (auth.currentUser != null){
                Log.i("TAG502", "before delete: ${auth.currentUser!!.email}")
                auth.signOut()
            }

            auth.signInWithEmailAndPassword(currentAccount!!.email!!, currentAccount!!.password!!)
                .addOnSuccessListener {
                    Log.i("TAG502", "sign in delete account: ${auth.currentUser!!.email}")
                    auth.currentUser!!.delete()
                        .addOnSuccessListener {
                            val progressDialog = createProgressDialog("Deleting an account...")
                            onlineAccountViewModel.deleteAccount(currentAccount!!)
                            onlineAccountViewModel.deleteAccount.observe(viewLifecycleOwner){
                                when (it) {
                                    is UiState.Loading -> {
                                        progressDialog.show()
                                    }
                                    is UiState.Failure -> {
                                        progressDialog.cancel()
                                        toast("$it")
                                    }
                                    is UiState.Success -> {
                                        auth.signInWithEmailAndPassword(adminAccount!!.email!!, adminAccount!!.password!!)
                                            .addOnSuccessListener {
                                                Log.i("TAG502", "after delete: ${auth.currentUser!!.email}")
                                            }
                                        progressDialog.cancel()
                                        toast(it.data)
                                        currentAccount = null
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            Firebase.auth.signInWithEmailAndPassword(adminAccount!!.email!!, adminAccount!!.password!!)
                                .addOnSuccessListener {
                                    Log.i("TAG502", "after delete: ${auth.currentUser!!.email}")
                                }
                        }

                }


        }

        binding.updateBtn.setOnClickListener {

            val dialog = createProgressDialog("Updating an account")

            if (currentAccount == null){
                toast("Please pick an account to update...")
                return@setOnClickListener
            }

            val name = binding.nameEt.text.toString()
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()
            val role = binding.roleEt.text.toString()

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

            if (name != currentAccount!!.name){
                dialog.show()
                currentAccount!!.name = name
                updateAccount(currentAccount!!)
                dialog.cancel()
            }
            if (role != currentAccount!!.role){
                dialog.show()
                currentAccount!!.role = role
                updateAccount(currentAccount!!)
                dialog.cancel()
            }

            if (imgUri != null){
                val progressDialog = createProgressDialog("Updating an account's image...")
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
                            currentAccount!!.imgFilePath = it.data.toString()
                            updateAccount(currentAccount!!)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }

                if (currentAccount!!.imgFilePath!!.isNotEmpty()){
                    val imgRef = FirebaseStorage
                        .getInstance()
                        .getReferenceFromUrl(currentAccount!!.imgFilePath.toString())
                    imgRef.delete()
                        .addOnSuccessListener {
                            toast("Deleted old image")
                        }
                        .addOnFailureListener {
                            toast("$it")
                        }
                }
            }

            if (email != currentAccount!!.email){

                viewLifecycleOwner.lifecycleScope.launch {
                    dialog.show()
                    val auth = Firebase.auth

                    if (auth.currentUser != null){
                        Log.i("TAG502", "before update: ${auth.currentUser!!.email}")
                        auth.signOut()
                    }

                    auth.signInWithEmailAndPassword(currentAccount!!.email!!, currentAccount!!.password!!)
                        .addOnSuccessListener {
                            viewLifecycleOwner.lifecycleScope.launch {

                                dialog.show()
                                currentAccount!!.email = email
                                updateAccount(currentAccount!!)
                                dialog.cancel()

                                val user = auth.currentUser
                                user!!.updateEmail(email)
                                    .addOnSuccessListener {
                                        auth.signOut()
                                        auth.signInWithEmailAndPassword(adminAccount!!.email!!, adminAccount!!.password!!)
                                            .addOnSuccessListener {
                                                Log.i("TAG502", "after update: ${auth.currentUser!!.email}")
                                                dialog.cancel()
                                            }
                                            .addOnFailureListener {
                                                dialog.cancel()
                                            }
                                    }
                                    .addOnFailureListener {
                                        auth.signOut()
                                        auth.signInWithEmailAndPassword(adminAccount!!.email!!, adminAccount!!.password!!)
                                            .addOnSuccessListener {
                                                Log.i("TAG502", "after update: ${auth.currentUser!!.email}")
                                                dialog.cancel()
                                            }
                                            .addOnFailureListener {
                                                dialog.cancel()
                                            }
                                    }
                            }
                        }
                        .addOnFailureListener {
                            dialog.cancel()
                        }
                }

            }

            if (password != currentAccount!!.password){

                viewLifecycleOwner.lifecycleScope.launch {
                    dialog.show()
                    val auth = Firebase.auth

                    if (auth.currentUser != null){
                        Log.i("TAG502", "before update: ${auth.currentUser!!.email}")
                        auth.signOut()
                    }

                    auth.signInWithEmailAndPassword(currentAccount!!.email!!, currentAccount!!.password!!)
                        .addOnSuccessListener {
                            viewLifecycleOwner.lifecycleScope.launch {

                                dialog.show()
                                currentAccount!!.password = password
                                updateAccount(currentAccount!!)
                                dialog.cancel()

                                val user = auth.currentUser
                                user!!.updatePassword(password)
                                    .addOnSuccessListener {
                                        auth.signOut()
                                        auth.signInWithEmailAndPassword(adminAccount!!.email!!, adminAccount!!.password!!)
                                            .addOnSuccessListener {
                                                Log.i("TAG502", "after update: ${auth.currentUser!!.email}")
                                                dialog.cancel()
                                            }
                                            .addOnFailureListener {
                                                dialog.cancel()
                                            }
                                    }
                                    .addOnFailureListener {
                                        auth.signOut()
                                        auth.signInWithEmailAndPassword(adminAccount!!.email!!, adminAccount!!.password!!)
                                            .addOnSuccessListener {
                                                Log.i("TAG502", "after update: ${auth.currentUser!!.email}")
                                                dialog.cancel()
                                            }
                                            .addOnFailureListener {
                                                dialog.cancel()
                                            }
                                    }
                            }
                        }
                        .addOnFailureListener {
                            dialog.cancel()
                        }
                }

            }
        }
    }

    private fun addAccount(account: OnlineAccount) {
        onlineAccountViewModel.addAccount(account)
        onlineAccountViewModel.addAccount.observe(viewLifecycleOwner){
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
                binding.imgFile.setImageURI(imgUri)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateAccount(account: OnlineAccount){
        onlineAccountViewModel.updateAccount(account)
        onlineAccountViewModel.updateAccount.observe(viewLifecycleOwner){
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
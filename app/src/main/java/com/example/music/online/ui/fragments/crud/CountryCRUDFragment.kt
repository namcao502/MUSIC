package com.example.music.online.ui.fragments.crud

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.FragmentCountryCrudBinding
import com.example.music.online.data.models.OnlineCountry
import com.example.music.online.ui.activities.SongManagerActivity
import com.example.music.online.viewModels.FirebaseViewModel
import com.example.music.online.viewModels.OnlineCountryViewModel
import com.example.music.utils.FireStoreCollection
import com.example.music.utils.UiState
import com.example.music.utils.createProgressDialog
import com.example.music.utils.toast
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileNotFoundException
import java.util.*

@AndroidEntryPoint
class CountryCRUDFragment : Fragment() {

    private var _binding: FragmentCountryCrudBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onlineCountryViewModel: OnlineCountryViewModel by viewModels()

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private var imgUri: Uri? = null

    private var currentCountry: OnlineCountry? = null

    private var countries: List<OnlineCountry> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCountryCrudBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onlineCountryViewModel.getAllCountries()
        onlineCountryViewModel.country.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    countries = it.data
                    binding.listView.adapter = ArrayAdapter(requireContext(),
                        androidx.appcompat.R.layout.
                        support_simple_spinner_dropdown_item, countries)
                }
            }
        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(text: String): Boolean {
                filterCountry(text.trim())
                return false
            }

            override fun onQueryTextChange(text: String): Boolean {
                filterCountry(text.trim())
                return false
            }

        })

        binding.resetBtn.setOnClickListener {
            binding.nameEt.setText("")
            imgUri = null
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
        }

        binding.imgFile.setOnLongClickListener {
            if (currentCountry!!.imgFilePath!!.isNotEmpty()){
                val imgRef = FirebaseStorage
                    .getInstance()
                    .getReferenceFromUrl(currentCountry!!.imgFilePath.toString())
                imgRef.delete()
                    .addOnSuccessListener {
                        toast("Deleted ${currentCountry!!.name} old image")
                        currentCountry!!.imgFilePath = ""
                        updateCountry(currentCountry!!)
                    }
                    .addOnFailureListener {
                        toast("$it")
                    }
            }
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
            return@setOnLongClickListener true
        }

        binding.listView.setOnItemClickListener { _, _, i, _ ->

            currentCountry = countries[i]
            binding.nameEt.setText(currentCountry!!.name)

            if (currentCountry!!.imgFilePath!!.isNotEmpty()){
                Glide.with(requireContext()).load(currentCountry!!.imgFilePath).into(binding.imgFile)
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

            if (name.isEmpty()){
                toast("Please give it a name...")
                return@setOnClickListener
            }

            val country = OnlineCountry("", name, emptyList(), "")

            if (imgUri != null){
                val progressDialog = createProgressDialog("Adding a country's image...")
                firebaseViewModel.uploadSingleImageFile("Country Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            country.imgFilePath = it.data.toString()
                            addCountry(country)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }
            }
            else {
                addCountry(country)
            }
        }

        binding.deleteBtn.setOnClickListener {
            if (currentCountry == null){
                toast("Please pick a country to delete...")
                return@setOnClickListener
            }
            val progressDialog = createProgressDialog("Deleting a country...")
            onlineCountryViewModel.deleteCountry(currentCountry!!)
            onlineCountryViewModel.deleteCountry.observe(viewLifecycleOwner){
                when (it) {
                    is UiState.Loading -> {
                        progressDialog.show()
                    }
                    is UiState.Failure -> {
                        progressDialog.cancel()
                        toast("$it")
                    }
                    is UiState.Success -> {
                        progressDialog.cancel()
                        toast(it.data)
                        currentCountry = null
                    }
                }
            }
        }

        binding.updateBtn.setOnClickListener {

            if (currentCountry == null){
                toast("Please pick an album to update...")
                return@setOnClickListener
            }

            val name = binding.nameEt.text.toString()
            if (name.isEmpty()){
                toast("Please give it a name...")
                return@setOnClickListener
            }
            currentCountry!!.name = name

            updateCountry(currentCountry!!)

            if (imgUri != null){
                val progressDialog = createProgressDialog("Updating a country's image...")
                firebaseViewModel.uploadSingleImageFile("Country Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            currentCountry!!.imgFilePath = it.data.toString()
                            updateCountry(currentCountry!!)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }

                if (currentCountry!!.imgFilePath!!.isNotEmpty()){
                    val imgRef = FirebaseStorage
                        .getInstance()
                        .getReferenceFromUrl(currentCountry!!.imgFilePath.toString())
                    imgRef.delete()
                        .addOnSuccessListener {
                            toast("Deleted old image")
                        }
                        .addOnFailureListener {
                            toast("$it")
                        }
                }
            }
        }

        binding.songMngBtn.setOnClickListener {
            if (currentCountry == null){
                toast("Please pick an album...")
                return@setOnClickListener
            }
            val intent = Intent(requireContext(), SongManagerActivity::class.java)
            intent.putExtra(FireStoreCollection.MODEL_NAME, FireStoreCollection.COUNTRY)
            intent.putExtra(FireStoreCollection.MODEL_ID, currentCountry!!.id)
            intent.putExtra(FireStoreCollection.MODEL_SONG_LIST, currentCountry!!.songs!! as ArrayList)
            startActivity(intent)
        }

    }

    private fun filterCountry(text: String) {
        val filter: ArrayList<OnlineCountry> = ArrayList()

        for (item in countries) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
        }
        if (text.isEmpty()){
            binding.listView.adapter = ArrayAdapter(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, countries)
        }
        else {
            binding.listView.adapter = ArrayAdapter(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, filter)
        }
    }

    private fun addCountry(country: OnlineCountry) {
        onlineCountryViewModel.addCountry(country)
        onlineCountryViewModel.addCountry.observe(viewLifecycleOwner){
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

    private fun updateCountry(country: OnlineCountry){
        onlineCountryViewModel.updateCountry(country)
        onlineCountryViewModel.updateCountry.observe(viewLifecycleOwner){
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
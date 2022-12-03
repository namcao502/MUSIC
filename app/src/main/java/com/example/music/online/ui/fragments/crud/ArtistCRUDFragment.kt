package com.example.music.online.ui.fragments.crud

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.databinding.FragmentArtistCrudBinding
import com.example.music.online.data.models.OnlineArtist
import com.example.music.online.ui.activities.SongManagerActivity
import com.example.music.online.viewModels.FirebaseViewModel
import com.example.music.online.viewModels.OnlineArtistViewModel
import com.example.music.utils.FireStoreCollection
import com.example.music.utils.UiState
import com.example.music.utils.createProgressDialog
import com.example.music.utils.toast
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileNotFoundException
import java.util.*

@AndroidEntryPoint
class ArtistCRUDFragment : Fragment() {

    private var _binding: FragmentArtistCrudBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onlineArtistViewModel: OnlineArtistViewModel by viewModels()

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private var imgUri: Uri? = null

    private var currentArtist: OnlineArtist? = null

    private var artists: List<OnlineArtist> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentArtistCrudBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun filterArtist(text: String) {
        val filter: ArrayList<OnlineArtist> = ArrayList()

        for (item in artists) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
        }
        if (text.isEmpty()){
            binding.listView.adapter = ArrayAdapter(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, artists)
        }
        else {
            binding.listView.adapter = ArrayAdapter(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, filter)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onlineArtistViewModel.getAllArtists()
        onlineArtistViewModel.artist.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    artists = it.data
                    binding.listView.adapter = ArrayAdapter(requireContext(),
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, artists)

                }
            }
        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(text: String): Boolean {
                filterArtist(text.trim())
                return false
            }

            override fun onQueryTextChange(text: String): Boolean {
                filterArtist(text.trim())
                return false
            }

        })

        binding.resetBtn.setOnClickListener {
            binding.nameEt.setText("")
            imgUri = null
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
        }

        binding.imgFile.setOnLongClickListener {
            if (currentArtist!!.imgFilePath!!.isNotEmpty()){
                val imgRef = FirebaseStorage
                    .getInstance()
                    .getReferenceFromUrl(currentArtist!!.imgFilePath.toString())
                imgRef.delete()
                    .addOnSuccessListener {
                        toast("Deleted ${currentArtist!!.name} old image")
                        currentArtist!!.imgFilePath = ""
                        updateArtist(currentArtist!!)
                    }
                    .addOnFailureListener {
                        toast("$it")
                    }
            }
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
            return@setOnLongClickListener true
        }

        binding.listView.setOnItemClickListener { _, _, i, _ ->

            currentArtist = artists[i]
            binding.nameEt.setText(currentArtist!!.name)

            if (currentArtist!!.imgFilePath!!.isNotEmpty()){
                Glide.with(requireContext()).load(currentArtist!!.imgFilePath).into(binding.imgFile)
            }
            else {
                binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
            }

        }

        binding.imgFile.setOnClickListener {
            imageChooser()
        }

        binding.addBtn.setOnClickListener {
            val name = binding.nameEt.text.toString()

            if (name.isEmpty()){
                toast("Please give it a name...")
                return@setOnClickListener
            }

            val artist = OnlineArtist("", name, emptyList(), "")

            if (imgUri != null){
                val progressDialog = createProgressDialog("Adding an artist's image...")
                firebaseViewModel.uploadSingleImageFile("Artist Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            artist.imgFilePath = it.data.toString()
                            addArtist(artist)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }
            }
            else {
                addArtist(artist)
            }
        }

        binding.deleteBtn.setOnClickListener {
            if (currentArtist == null){
                toast("Please pick an artist to delete...")
                return@setOnClickListener
            }
            val progressDialog = createProgressDialog("Deleting an artist...")
            onlineArtistViewModel.deleteArtist(currentArtist!!)
            onlineArtistViewModel.deleteArtist.observe(viewLifecycleOwner){
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
                        currentArtist = null
                    }
                }
            }
        }

        binding.updateBtn.setOnClickListener {
            if (currentArtist == null){
                toast("Please pick an artist to update...")
                return@setOnClickListener
            }

            val name = binding.nameEt.text.toString()
            if (name.isEmpty()){
                toast("Please give it a name...")
                return@setOnClickListener
            }
            currentArtist!!.name = name

            updateArtist(currentArtist!!)

            if (imgUri != null){
                val progressDialog = createProgressDialog("Updating an artist's image...")
                firebaseViewModel.uploadSingleImageFile("Artist Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            currentArtist!!.imgFilePath = it.data.toString()
                            updateArtist(currentArtist!!)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }

                if (currentArtist!!.imgFilePath!!.isNotEmpty()){
                    val imgRef = FirebaseStorage
                        .getInstance()
                        .getReferenceFromUrl(currentArtist!!.imgFilePath.toString())
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
            if (currentArtist == null){
                toast("Please pick an artist...")
                return@setOnClickListener
            }
            val intent = Intent(requireContext(), SongManagerActivity::class.java)
            intent.putExtra(FireStoreCollection.MODEL_NAME, FireStoreCollection.ARTIST)
            intent.putExtra(FireStoreCollection.MODEL_ID, currentArtist!!.id)
            intent.putExtra(FireStoreCollection.MODEL_SONG_LIST, currentArtist!!.songs!! as ArrayList)
            startActivity(intent)
        }

    }

    private fun addArtist(artist: OnlineArtist) {
        onlineArtistViewModel.addArtist(artist)
        onlineArtistViewModel.addArtist.observe(viewLifecycleOwner){
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
                    binding.imgFile.setImageURI(imgUri)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateArtist(artist: OnlineArtist){
        onlineArtistViewModel.updateArtist(artist)
        onlineArtistViewModel.updateArtist.observe(viewLifecycleOwner){
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
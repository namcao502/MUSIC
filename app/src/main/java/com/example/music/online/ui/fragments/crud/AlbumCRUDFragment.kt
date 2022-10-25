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
import com.example.music.databinding.FragmentAlbumCrudBinding
import com.example.music.online.data.models.OnlineAlbum
import com.example.music.online.ui.activities.SongManagerActivity
import com.example.music.online.viewModels.FirebaseViewModel
import com.example.music.online.viewModels.OnlineAlbumViewModel
import com.example.music.utils.FireStoreCollection
import com.example.music.utils.UiState
import com.example.music.utils.createProgressDialog
import com.example.music.utils.toast
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileNotFoundException
import java.util.*


@AndroidEntryPoint
class AlbumCRUDFragment : Fragment() {

    private var _binding: FragmentAlbumCrudBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onlineAlbumViewModel: OnlineAlbumViewModel by viewModels()

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private var imgUri: Uri? = null

    private var currentAlbum: OnlineAlbum? = null

    private var albums: List<OnlineAlbum> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAlbumCrudBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onlineAlbumViewModel.getAllAlbums()
        onlineAlbumViewModel.album.observe(viewLifecycleOwner){ album ->
            when(album){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {

                    albums = album.data
                    binding.listView.adapter = ArrayAdapter(requireContext(),
                        androidx.appcompat.R.layout.
                        support_simple_spinner_dropdown_item, albums)

                }
            }
        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(text: String): Boolean {
                filterAlbum(text)
                return false
            }

            override fun onQueryTextChange(text: String): Boolean {
                filterAlbum(text)
                return false
            }

        })

        binding.resetBtn.setOnClickListener {
            binding.nameEt.setText("")
            imgUri = null
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
        }

        binding.imgFile.setOnLongClickListener {
            if (currentAlbum!!.imgFilePath!!.isNotEmpty()){
                val imgRef = FirebaseStorage
                    .getInstance()
                    .getReferenceFromUrl(currentAlbum!!.imgFilePath.toString())
                imgRef.delete()
                    .addOnSuccessListener {
                        toast("Deleted ${currentAlbum!!.name} old image")
                        currentAlbum!!.imgFilePath = ""
                        updateAlbum(currentAlbum!!)
                    }
                    .addOnFailureListener {
                        toast("$it")
                    }
            }
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
            return@setOnLongClickListener true
        }

        binding.listView.setOnItemClickListener { _, _, i, _ ->

            currentAlbum = albums[i]
            binding.nameEt.setText(currentAlbum!!.name)

            if (currentAlbum!!.imgFilePath!!.isNotEmpty()){
                Glide.with(requireContext()).load(currentAlbum!!.imgFilePath).into(binding.imgFile)
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

            val album = OnlineAlbum("", name, emptyList(), "")

            if (imgUri != null){
                val progressDialog = createProgressDialog("Adding an album's image...")
                firebaseViewModel.uploadSingleImageFile("Album Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            album.imgFilePath = it.data.toString()
                            addAlbum(album)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }
            }
            else {
                addAlbum(album)
            }
        }

        binding.deleteBtn.setOnClickListener {
            if (currentAlbum == null){
                toast("Please pick an album to delete...")
                return@setOnClickListener
            }
            val progressDialog = createProgressDialog("Deleting an album...")
            onlineAlbumViewModel.deleteAlbum(currentAlbum!!)
            onlineAlbumViewModel.deleteAlbum.observe(viewLifecycleOwner){
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
                        currentAlbum = null
                    }
                }
            }
        }

        binding.updateBtn.setOnClickListener {

            if (currentAlbum == null){
                toast("Please pick an album to update...")
                return@setOnClickListener
            }

            val name = binding.nameEt.text.toString()
            if (name.isEmpty()){
                toast("Please give it a name...")
                return@setOnClickListener
            }
            currentAlbum!!.name = name

            updateAlbum(currentAlbum!!)

            if (imgUri != null){
                val progressDialog = createProgressDialog("Updating an album's image...")
                firebaseViewModel.uploadSingleImageFile("Album Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            currentAlbum!!.imgFilePath = it.data.toString()
                            updateAlbum(currentAlbum!!)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }

                if (currentAlbum!!.imgFilePath!!.isNotEmpty()){
                    val imgRef = FirebaseStorage
                        .getInstance()
                        .getReferenceFromUrl(currentAlbum!!.imgFilePath.toString())
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
            if (currentAlbum == null){
                toast("Please pick an album...")
                return@setOnClickListener
            }
            val intent = Intent(requireContext(), SongManagerActivity::class.java)
            intent.putExtra(FireStoreCollection.MODEL_NAME, FireStoreCollection.ALBUM)
            intent.putExtra(FireStoreCollection.MODEL_ID, currentAlbum!!.id)
            intent.putExtra(FireStoreCollection.MODEL_SONG_LIST, currentAlbum!!.songs!! as ArrayList)
            startActivity(intent)
        }

    }

    private fun filterAlbum(text: String) {
        val filter: ArrayList<OnlineAlbum> = ArrayList()
        for (item in albums) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
        }
        if (text.isEmpty()){
            binding.listView.adapter = ArrayAdapter(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, albums)
        }
        else {
            binding.listView.adapter = ArrayAdapter(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, filter)
        }
    }

    private fun addAlbum(album: OnlineAlbum) {
        onlineAlbumViewModel.addAlbum(album)
        onlineAlbumViewModel.addAlbum.observe(viewLifecycleOwner){
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
            try {
                imgUri = result.data?.data
                binding.imgFile.setImageURI(imgUri)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateAlbum(album: OnlineAlbum){
        onlineAlbumViewModel.updateAlbum(album)
        onlineAlbumViewModel.updateAlbum.observe(viewLifecycleOwner){
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
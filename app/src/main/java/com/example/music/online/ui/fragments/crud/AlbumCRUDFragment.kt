package com.example.music.online.ui.fragments.crud

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.utils.UiState
import com.example.music.online.data.models.OnlineAlbum
import com.example.music.online.data.models.OnlineSong
import com.example.music.databinding.FragmentAlbumCrudBinding
import com.example.music.utils.createDialog
import com.example.music.utils.createProgressDialog
import com.example.music.utils.toast
import com.example.music.online.viewModels.FirebaseViewModel
import com.example.music.online.viewModels.OnlineAlbumViewModel
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileNotFoundException


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

        var albums: List<OnlineAlbum> = emptyList()

        onlineAlbumViewModel.getAllAlbums()
        onlineAlbumViewModel.album.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    albums = it.data
                    with(binding.listView){
                        adapter = ArrayAdapter(requireContext(),
                            androidx.appcompat.R.layout.
                            support_simple_spinner_dropdown_item, albums)
                    }
                }
            }
        }

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

            val dialog = createDialog()

            val allSongs = dialog.findViewById<ListView>(R.id.all_song_lv)
            val currentSongs = dialog.findViewById<ListView>(R.id.this_lv)

            var current: List<OnlineSong> = emptyList()

            if (currentAlbum!!.songs!!.isNotEmpty()){
                firebaseViewModel.getSongFromListSongID(currentAlbum!!.songs!!)
                firebaseViewModel.songFromID.observe(viewLifecycleOwner){
                    when(it) {
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
                            current = it.data
                            currentSongs.adapter = ArrayAdapter(requireContext(),
                                androidx.appcompat.R.layout.
                                support_simple_spinner_dropdown_item,
                                current)
                        }
                    }
                }
            }

            var all: List<OnlineSong> = emptyList()

            firebaseViewModel.getAllSongs()
            firebaseViewModel.song.observe(viewLifecycleOwner){
                when(it) {
                    is UiState.Loading -> {

                    }
                    is UiState.Failure -> {

                    }
                    is UiState.Success -> {
                        all = it.data
                        allSongs.adapter = ArrayAdapter(requireContext(),
                            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, all)
                    }
                }
            }

            allSongs.setOnItemClickListener { _, _, i, _ ->
                val temp = currentAlbum!!.songs as ArrayList
                temp.add(all[i].id!!)
                currentAlbum!!.songs = temp
                updateAlbum(currentAlbum!!)

                //reload
                if (currentAlbum!!.songs!!.isNotEmpty()){
                    firebaseViewModel.getSongFromListSongID(currentAlbum!!.songs!!)
                    firebaseViewModel.songFromID.observe(viewLifecycleOwner){
                        when(it) {
                            is UiState.Loading -> {

                            }
                            is UiState.Failure -> {

                            }
                            is UiState.Success -> {
                                current = it.data
                                currentSongs.adapter = ArrayAdapter(requireContext(),
                                    androidx.appcompat.R.layout.
                                    support_simple_spinner_dropdown_item,
                                    current)
                            }
                        }
                    }
                }
                else {
                    current = emptyList()
                    currentSongs.adapter = ArrayAdapter(requireContext(),
                        androidx.appcompat.R.layout.
                        support_simple_spinner_dropdown_item,
                        current)
                }
            }

            currentSongs.setOnItemClickListener { _, _, i, _ ->

                val temp = currentAlbum!!.songs as ArrayList
                temp.remove(current[i].id!!)
                currentAlbum!!.songs = temp
                updateAlbum(currentAlbum!!)

                //reload
                if (currentAlbum!!.songs!!.isNotEmpty()){
                    firebaseViewModel.getSongFromListSongID(currentAlbum!!.songs!!)
                    firebaseViewModel.songFromID.observe(viewLifecycleOwner){
                        when(it) {
                            is UiState.Loading -> {

                            }
                            is UiState.Failure -> {

                            }
                            is UiState.Success -> {
                                current = it.data
                                currentSongs.adapter = ArrayAdapter(requireContext(),
                                    androidx.appcompat.R.layout.
                                    support_simple_spinner_dropdown_item,
                                    current)
                            }
                        }
                    }
                }
                else {
                    current = emptyList()
                    currentSongs.adapter = ArrayAdapter(requireContext(),
                        androidx.appcompat.R.layout.
                        support_simple_spinner_dropdown_item,
                        current)
                }
            }

            dialog.show()
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
            // There are no request codes
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
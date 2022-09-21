package com.example.music.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.UiState
import com.example.music.data.models.online.OnlineSong
import com.example.music.databinding.FragmentHomeCrudBinding
import com.example.music.databinding.FragmentSongCrudBinding
import com.example.music.utils.Permission
import com.example.music.utils.createProgressDialog
import com.example.music.utils.toast
import com.example.music.viewModels.FirebaseViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileNotFoundException


@AndroidEntryPoint
class SongCRUDFragment : Fragment() {

    private var _binding: FragmentSongCrudBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private var imgFilePath: String? = ""
    private var filePath: String? = ""
    private var songUri: Uri? = null
    private var imgUri: Uri? = null
    private var isSong = true

    private var currentSong: OnlineSong? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSongCrudBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var songs: List<OnlineSong> = emptyList()

        firebaseViewModel.getAllSongs()
        firebaseViewModel.song.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    songs = it.data
                    with(binding.listView){
                        adapter = ArrayAdapter(requireContext(),
                            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, songs)
                    }
                }
            }
        }

        binding.listView.setOnItemClickListener { adapterView, view, i, l ->
            binding.nameEt.setText(songs[i].name)
            currentSong = songs[i]

            imgFilePath = if (songs[i].imgFilePath!!.isNotEmpty()){
                Glide.with(requireContext()).load(songs[i].imgFilePath).into(binding.imgFile)
                songs[i].imgFilePath!!
            } else {
                ""
            }

            filePath = if (songs[i].filePath!!.isNotEmpty()){
                binding.songFile.setImageResource(R.drawable.icons8_audio_file_100)
                songs[i].filePath!!
            } else {
                ""
            }
        }

        binding.imgFile.setOnClickListener {
            isSong = false
            val intent = Intent()
            intent.type = "Song Images/"
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
        }

        binding.songFile.setOnClickListener {
            isSong = true
            val intent = Intent()
            intent.type = "Songs/"
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(Intent.createChooser(intent, "Select Song"))
        }

        binding.addBtn.setOnClickListener {
            val name = binding.nameEt.text.toString()
            if (name.isEmpty()){
                toast("Please give it a name...")
                return@setOnClickListener
            }
            if (songUri == null){
                toast("Please pick a song...")
                return@setOnClickListener
            }

            val progressDialog = createProgressDialog("Adding a new song")
            firebaseViewModel.uploadSingleSongFile(name, songUri!!){
                when (it) {
                    is UiState.Loading -> {
                        progressDialog.show()
                    }
                    is UiState.Failure -> {
                        progressDialog.cancel()
                        toast("$it")
                    }
                    is UiState.Success -> {
                        addSong(OnlineSong("", name, "", it.data.toString()))
                        toast("Added $name to Database!")
                        songUri = null
                        progressDialog.cancel()
                    }
                }
            }

        }

        binding.deleteBtn.setOnClickListener {
            if (currentSong == null){
                toast("Please pick a song to delete...")
                return@setOnClickListener
            }
            val progressDialog = createProgressDialog("Deleting a song...")
            firebaseViewModel.deleteSong(currentSong!!)
            firebaseViewModel.deleteSong.observe(viewLifecycleOwner){
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
                        currentSong = null
                    }
                }
            }
        }

        binding.updateBtn.setOnClickListener {
            if (currentSong == null){
                toast("Please pick a song to update...")
                return@setOnClickListener
            }

            val name = binding.nameEt.text.toString()
            if (name.isEmpty()){
                toast("Please give it a name...")
                return@setOnClickListener
            }
            val updatedSong = currentSong
            updatedSong!!.name = name

            updateSong(currentSong!!)

            if (songUri != null){
                //delete old mp3 file
                val songRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentSong!!.filePath.toString())
                songRef.delete()
                    .addOnSuccessListener {
                        //upload new mp3 file
                        val progressDialog = createProgressDialog("Updating a raw song...")
                        firebaseViewModel.uploadSingleSongFile(name, songUri!!){
                            when (it) {
                                is UiState.Loading -> {
                                    progressDialog.show()
                                }
                                is UiState.Failure -> {
                                    progressDialog.cancel()
                                    toast("$it")
                                }
                                is UiState.Success -> {
                                    updatedSong.filePath = it.data.toString()
                                    updateSong(currentSong!!)
                                    songUri = null
                                    progressDialog.cancel()
                                }
                            }
                        }
                    }
                    .addOnFailureListener {}
            }
            if (imgUri != null){
                if (currentSong!!.imgFilePath!!.isEmpty()){
                    val progressDialog = createProgressDialog("Updating a song's image...")
                    firebaseViewModel.uploadSingleImageFile(name, imgUri!!){
                        when (it) {
                            is UiState.Loading -> {
                                progressDialog.show()
                            }
                            is UiState.Failure -> {
                                progressDialog.cancel()
                                toast("$it")
                            }
                            is UiState.Success -> {
                                updatedSong.imgFilePath = it.data.toString()
                                updateSong(currentSong!!)
                                imgUri = null
                                progressDialog.cancel()
                            }
                        }
                    }
                }
                else {
                    val imgRef = FirebaseStorage
                        .getInstance()
                        .getReferenceFromUrl(currentSong!!.imgFilePath.toString())
                    imgRef.delete()
                        .addOnSuccessListener {
                            val progressDialog = createProgressDialog("Updating a song's image...")
                            firebaseViewModel.uploadSingleImageFile(name, imgUri!!){
                                when (it) {
                                    is UiState.Loading -> {
                                        progressDialog.show()
                                    }
                                    is UiState.Failure -> {
                                        progressDialog.cancel()
                                        toast("$it")
                                    }
                                    is UiState.Success -> {
                                        updatedSong.imgFilePath = it.data.toString()
                                        updateSong(currentSong!!)
                                        imgUri = null
                                        progressDialog.cancel()
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {  }
                }
            }
        }

    }

    fun addSong(song: OnlineSong){
        firebaseViewModel.addSong(song)
        firebaseViewModel.addSong.observe(viewLifecycleOwner){
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

    private fun updateSong(song: OnlineSong){
        firebaseViewModel.updateSong(song)
        firebaseViewModel.updateSong.observe(viewLifecycleOwner){
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
                if (isSong){
                    songUri = result.data?.data
                    binding.songFile.setImageResource(R.drawable.icons8_audio_file_100)
                }
                else {
                    imgUri = result.data?.data
                    binding.imgFile.setImageURI(imgUri)
                }
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
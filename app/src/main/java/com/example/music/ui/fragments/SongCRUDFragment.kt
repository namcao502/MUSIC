package com.example.music.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.music.databinding.FragmentSongCrudBinding
import com.example.music.utils.createProgressDialog
import com.example.music.utils.toast
import com.example.music.viewModels.online.OnlineSongViewModel
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileNotFoundException

@AndroidEntryPoint
class SongCRUDFragment : Fragment() {

    private var _binding: FragmentSongCrudBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onlineSongViewModel: OnlineSongViewModel by viewModels()

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

        onlineSongViewModel.getAllSongs()
        onlineSongViewModel.song.observe(viewLifecycleOwner){
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

        binding.resetBtn.setOnClickListener {
            binding.nameEt.setText("")
            songUri = null
            imgUri = null
            currentSong = null
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
            binding.songFile.setImageResource(R.drawable.icons8_remove_document_64)
        }

        binding.imgFile.setOnLongClickListener {
            if (currentSong!!.imgFilePath!!.isNotEmpty()){
                val imgRef = FirebaseStorage
                    .getInstance()
                    .getReferenceFromUrl(currentSong!!.imgFilePath.toString())
                imgRef.delete()
                    .addOnSuccessListener {
                        toast("Deleted ${currentSong!!.name} old image")
                        currentSong!!.imgFilePath = ""
                        updateSong(currentSong!!)
                    }
                    .addOnFailureListener {
                        toast("$it")
                    }
            }
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
            return@setOnLongClickListener true
        }

        binding.listView.setOnItemClickListener { _, _, i, _ ->

            currentSong = songs[i]
            binding.nameEt.setText(currentSong!!.name)

            if (currentSong!!.imgFilePath!!.isNotEmpty()){
                Glide.with(requireContext()).load(currentSong!!.imgFilePath).into(binding.imgFile)
            }
            else {
                binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
            }

             if (currentSong!!.filePath!!.isNotEmpty()){
                binding.songFile.setImageResource(R.drawable.icons8_audio_file_100)
            }
            else {
                 binding.songFile.setImageResource(R.drawable.icons8_remove_document_64)
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

            val song = OnlineSong("", name, "", "")

            if (imgUri != null){
                val progressDialog = createProgressDialog("Adding a song's image...")
                onlineSongViewModel.uploadSingleImageFile("Song Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            song.imgFilePath = it.data.toString()
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }
            }

            val progressDialog = createProgressDialog("Adding a new song")
            onlineSongViewModel.uploadSingleSongFile(name, songUri!!){
                when (it) {
                    is UiState.Loading -> {
                        progressDialog.show()
                    }
                    is UiState.Failure -> {
                        progressDialog.cancel()
                        toast("$it")
                    }
                    is UiState.Success -> {
                        song.filePath = it.data.toString()
                        addSong(song)
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
            onlineSongViewModel.deleteSong(currentSong!!)
            onlineSongViewModel.deleteSong.observe(viewLifecycleOwner){
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
            currentSong!!.name = name
            updateSong(currentSong!!)

            if (songUri != null){
                //delete old mp3 file
                val songRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentSong!!.filePath.toString())
                songRef.delete()
                    .addOnSuccessListener {
                        //upload new mp3 file
                        val progressDialog = createProgressDialog("Updating a raw song...")
                        onlineSongViewModel.uploadSingleSongFile(name, songUri!!){
                            when (it) {
                                is UiState.Loading -> {
                                    progressDialog.show()
                                }
                                is UiState.Failure -> {
                                    progressDialog.cancel()
                                    toast("$it")
                                }
                                is UiState.Success -> {
                                    currentSong!!.filePath = it.data.toString()
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
                val progressDialog = createProgressDialog("Updating a song's image...")
                onlineSongViewModel.uploadSingleImageFile("Song Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            currentSong!!.imgFilePath = it.data.toString()
                            updateSong(currentSong!!)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }

                if (currentSong!!.imgFilePath!!.isNotEmpty()){
                    val imgRef = FirebaseStorage
                        .getInstance()
                        .getReferenceFromUrl(currentSong!!.imgFilePath.toString())
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

    }

    fun addSong(song: OnlineSong){
        onlineSongViewModel.addSong(song)
        onlineSongViewModel.addSong.observe(viewLifecycleOwner){
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
        onlineSongViewModel.updateSong(song)
        onlineSongViewModel.updateSong.observe(viewLifecycleOwner){
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
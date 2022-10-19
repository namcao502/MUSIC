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
import com.example.music.online.data.models.OnlineGenre
import com.example.music.online.data.models.OnlineSong
import com.example.music.databinding.FragmentGenreCrudBinding
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.utils.createDialog
import com.example.music.utils.createProgressDialog
import com.example.music.utils.toast
import com.example.music.online.viewModels.FirebaseViewModel
import com.example.music.online.viewModels.OnlineGenreViewModel
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class GenreCRUDFragment : Fragment() {

    private var _binding: FragmentGenreCrudBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onlineGenreViewModel: OnlineGenreViewModel by viewModels()

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private var imgUri: Uri? = null

    private var currentGenre: OnlineGenre? = null

    private var genres: List<OnlineGenre> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGenreCrudBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun filterGenre(text: String) {
        val filter: ArrayList<OnlineGenre> = ArrayList()

        for (item in genres) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
        }
        if (text.isEmpty()){
            binding.listView.adapter = ArrayAdapter(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, genres)
        }
        else {
            binding.listView.adapter = ArrayAdapter(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, filter)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onlineGenreViewModel.getAllGenres()
        onlineGenreViewModel.genre.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    genres = it.data
                    with(binding.listView){
                        adapter = ArrayAdapter(requireContext(),
                            androidx.appcompat.R.layout.
                            support_simple_spinner_dropdown_item, genres)
                    }
                }
            }
        }

        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String): Boolean {
                filterGenre(p0)
                return false
            }

            override fun onQueryTextChange(text: String): Boolean {
                filterGenre(text)
                return false
            }

        })

        binding.resetBtn.setOnClickListener {
            binding.nameEt.setText("")
            imgUri = null
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
        }

        binding.imgFile.setOnLongClickListener {
            if (currentGenre!!.imgFilePath!!.isNotEmpty()){
                val imgRef = FirebaseStorage
                    .getInstance()
                    .getReferenceFromUrl(currentGenre!!.imgFilePath.toString())
                imgRef.delete()
                    .addOnSuccessListener {
                        toast("Deleted ${currentGenre!!.name} old image")
                        currentGenre!!.imgFilePath = ""
                        updateGenre(currentGenre!!)
                    }
                    .addOnFailureListener {
                        toast("$it")
                    }
            }
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
            return@setOnLongClickListener true
        }

        binding.listView.setOnItemClickListener { _, _, i, _ ->

            currentGenre = genres[i]
            binding.nameEt.setText(currentGenre!!.name)

            if (currentGenre!!.imgFilePath!!.isNotEmpty()){
                Glide.with(requireContext()).load(currentGenre!!.imgFilePath).into(binding.imgFile)
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

            val genre = OnlineGenre("", name, emptyList(), "")

            if (imgUri != null){
                val progressDialog = createProgressDialog("Adding a genre's image...")
                firebaseViewModel.uploadSingleImageFile("Genre Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            genre.imgFilePath = it.data.toString()
                            addGenre(genre)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }
            }
            else {
                addGenre(genre)
            }
        }

        binding.deleteBtn.setOnClickListener {
            if (currentGenre == null){
                toast("Please pick a genre to delete...")
                return@setOnClickListener
            }
            val progressDialog = createProgressDialog("Deleting a genre...")
            onlineGenreViewModel.deleteGenre(currentGenre!!)
            onlineGenreViewModel.deleteGenre.observe(viewLifecycleOwner){
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
                        currentGenre = null
                    }
                }
            }
        }

        binding.updateBtn.setOnClickListener {
            if (currentGenre == null){
                toast("Please pick a genre to update...")
                return@setOnClickListener
            }

            val name = binding.nameEt.text.toString()
            if (name.isEmpty()){
                toast("Please give it a name...")
                return@setOnClickListener
            }
            currentGenre!!.name = name

            updateGenre(currentGenre!!)

            if (imgUri != null){
                val progressDialog = createProgressDialog("Updating a genre's image...")
                firebaseViewModel.uploadSingleImageFile("Genre Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            currentGenre!!.imgFilePath = it.data.toString()
                            updateGenre(currentGenre!!)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }

                if (currentGenre!!.imgFilePath!!.isNotEmpty()){
                    val imgRef = FirebaseStorage
                        .getInstance()
                        .getReferenceFromUrl(currentGenre!!.imgFilePath.toString())
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

            if (currentGenre == null){
                toast("Please pick a genre...")
                return@setOnClickListener
            }

            val dialog = createDialog(R.layout.song_crud_dialog)

            val allSongs = dialog.findViewById<ListView>(R.id.all_song_lv)
            val currentSongs = dialog.findViewById<ListView>(R.id.this_lv)

            var current: List<OnlineSong> = emptyList()

            if (currentGenre!!.songs!!.isNotEmpty()){
                firebaseViewModel.getSongFromListSongID(currentGenre!!.songs!!)
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
                val temp = currentGenre!!.songs as ArrayList
                temp.add(all[i].id!!)
                currentGenre!!.songs = temp
                updateGenre(currentGenre!!)

                //reload
                if (currentGenre!!.songs!!.isNotEmpty()){
                    firebaseViewModel.getSongFromListSongID(currentGenre!!.songs!!)
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

                val temp = currentGenre!!.songs as ArrayList
                temp.remove(current[i].id!!)
                currentGenre!!.songs = temp
                updateGenre(currentGenre!!)

                //reload
                if (currentGenre!!.songs!!.isNotEmpty()){
                    firebaseViewModel.getSongFromListSongID(currentGenre!!.songs!!)
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

    private fun addGenre(genre: OnlineGenre) {
        onlineGenreViewModel.addGenre(genre)
        onlineGenreViewModel.addGenre.observe(viewLifecycleOwner){
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

    private fun updateGenre(genre: OnlineGenre){
        onlineGenreViewModel.updateGenre(genre)
        onlineGenreViewModel.updateGenre.observe(viewLifecycleOwner){
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
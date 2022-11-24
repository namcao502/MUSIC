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
import com.example.music.databinding.FragmentPlaylistCrudBinding
import com.example.music.online.data.models.OnlinePlaylist
import com.example.music.online.ui.activities.SongManagerActivity
import com.example.music.online.viewModels.FirebaseViewModel
import com.example.music.online.viewModels.OnlinePlaylistViewModel
import com.example.music.utils.FireStoreCollection
import com.example.music.utils.UiState
import com.example.music.utils.createProgressDialog
import com.example.music.utils.toast
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileNotFoundException
import java.util.*

@AndroidEntryPoint
class PlaylistCRUDFragment : Fragment() {

    private var _binding: FragmentPlaylistCrudBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onlinePlaylistViewModel: OnlinePlaylistViewModel by viewModels()

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private var imgUri: Uri? = null

    private var currentPlaylist: OnlinePlaylist? = null

    private var playlists: List<OnlinePlaylist> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPlaylistCrudBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun filterPlaylist(text: String) {
        val filter: ArrayList<OnlinePlaylist> = ArrayList()

        for (item in playlists) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
        }
        if (text.isEmpty()){
            binding.listView.adapter = ArrayAdapter(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, playlists)
        }
        else {
            binding.listView.adapter = ArrayAdapter(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, filter)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onlinePlaylistViewModel.getAllPlaylists()
        onlinePlaylistViewModel.playlist2.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    playlists = it.data
                    binding.listView.adapter = ArrayAdapter(requireContext(),
                        androidx.appcompat.R.layout.
                        support_simple_spinner_dropdown_item, playlists)

                }
            }
        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(text: String): Boolean {
                filterPlaylist(text.trim())
                return false
            }

            override fun onQueryTextChange(text: String): Boolean {
                filterPlaylist(text.trim())
                return false
            }

        })

        binding.resetBtn.setOnClickListener {
            binding.nameEt.setText("")
            imgUri = null
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
        }

        binding.imgFile.setOnLongClickListener {
            if (currentPlaylist!!.imgFilePath!!.isNotEmpty()){
                val imgRef = FirebaseStorage
                    .getInstance()
                    .getReferenceFromUrl(currentPlaylist!!.imgFilePath.toString())
                imgRef.delete()
                    .addOnSuccessListener {
                        toast("Deleted ${currentPlaylist!!.name} old image")
                        currentPlaylist!!.imgFilePath = ""
                        updatePlaylist(currentPlaylist!!)
                    }
                    .addOnFailureListener {
                        toast("$it")
                    }
            }
            binding.imgFile.setImageResource(R.drawable.icons8_artist_100)
            return@setOnLongClickListener true
        }

        binding.listView.setOnItemClickListener { _, _, i, _ ->

            currentPlaylist = playlists[i]
            binding.nameEt.setText(currentPlaylist!!.name)

            if (currentPlaylist!!.imgFilePath!!.isNotEmpty()){
                Glide.with(requireContext()).load(currentPlaylist!!.imgFilePath).into(binding.imgFile)
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

            val playlist = OnlinePlaylist("", name, emptyList(), "")

            if (imgUri != null){
                val progressDialog = createProgressDialog("Adding a playlist's image...")
                firebaseViewModel.uploadSingleImageFile("Playlist Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            playlist.imgFilePath = it.data.toString()
                            addPlaylist(playlist)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }
            }
            else {
                addPlaylist(playlist)
            }
        }

        binding.deleteBtn.setOnClickListener {
            if (currentPlaylist == null){
                toast("Please pick a playlist to delete...")
                return@setOnClickListener
            }
            val progressDialog = createProgressDialog("Deleting a playlist...")
            onlinePlaylistViewModel.deletePlaylist(currentPlaylist!!)
            onlinePlaylistViewModel.deletePlaylist2.observe(viewLifecycleOwner){
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
                        currentPlaylist = null
                    }
                }
            }
        }

        binding.updateBtn.setOnClickListener {
            if (currentPlaylist == null){
                toast("Please pick a playlist to update...")
                return@setOnClickListener
            }

            val name = binding.nameEt.text.toString()
            if (name.isEmpty()){
                toast("Please give it a name...")
                return@setOnClickListener
            }
            currentPlaylist!!.name = name

            updatePlaylist(currentPlaylist!!)

            if (imgUri != null){
                val progressDialog = createProgressDialog("Updating a playlist's image...")
                firebaseViewModel.uploadSingleImageFile("Playlist Images", name, imgUri!!){
                    when (it) {
                        is UiState.Loading -> {
                            progressDialog.show()
                        }
                        is UiState.Failure -> {
                            progressDialog.cancel()
                            toast("$it")
                        }
                        is UiState.Success -> {
                            currentPlaylist!!.imgFilePath = it.data.toString()
                            updatePlaylist(currentPlaylist!!)
                            imgUri = null
                            progressDialog.cancel()
                        }
                    }
                }

                if (currentPlaylist!!.imgFilePath!!.isNotEmpty()){
                    val imgRef = FirebaseStorage
                        .getInstance()
                        .getReferenceFromUrl(currentPlaylist!!.imgFilePath.toString())
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

            if (currentPlaylist == null){
                toast("Please pick a playlist...")
                return@setOnClickListener
            }

            val intent = Intent(requireContext(), SongManagerActivity::class.java)
            intent.putExtra(FireStoreCollection.MODEL_NAME, FireStoreCollection.PLAYLIST)
            intent.putExtra(FireStoreCollection.MODEL_ID, currentPlaylist!!.id)
            intent.putExtra(FireStoreCollection.MODEL_SONG_LIST, currentPlaylist!!.songs!! as ArrayList)
            startActivity(intent)
        }

    }

    private fun addPlaylist(playlist: OnlinePlaylist) {
        onlinePlaylistViewModel.addPlaylist(playlist)
        onlinePlaylistViewModel.addPlaylist2.observe(viewLifecycleOwner){
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

    private fun updatePlaylist(playlist: OnlinePlaylist){
        onlinePlaylistViewModel.updatePlaylist(playlist)
        onlinePlaylistViewModel.updatePlaylist2.observe(viewLifecycleOwner){
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
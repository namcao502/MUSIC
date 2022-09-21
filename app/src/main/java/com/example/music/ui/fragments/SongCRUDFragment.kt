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

        }

        binding.songFile.setOnClickListener {
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
                        toast("$it")
                    }
                    is UiState.Success -> {
                        addSong(OnlineSong("", name, "", it.data.toString()))
                        toast("Added $name to Database!")
                        progressDialog.cancel()
                    }
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


    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            try {
                songUri = result.data?.data
                binding.songFile.setImageResource(R.drawable.icons8_audio_file_100)
                Log.i("TAG502", "onActivityResult: $songUri")
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
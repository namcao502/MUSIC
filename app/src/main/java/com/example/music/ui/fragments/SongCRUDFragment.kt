package com.example.music.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.UiState
import com.example.music.data.models.online.OnlineSong
import com.example.music.databinding.FragmentHomeCrudBinding
import com.example.music.databinding.FragmentSongCrudBinding
import com.example.music.viewModels.FirebaseViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SongCRUDFragment : Fragment() {

    private var _binding: FragmentSongCrudBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val firebaseViewModel: FirebaseViewModel by viewModels()

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
            binding.idEt.setText(songs[i].id)
            binding.nameEt.setText(songs[i].name)
            if (songs[i].imgFilePath!!.isNotEmpty()){
                Glide.with(requireContext()).load(songs[i].imgFilePath).into(binding.imgFile)
            }
            if (songs[i].filePath!!.isNotEmpty()){
                binding.songFile.setImageResource(R.drawable.icons8_audio_file_100)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
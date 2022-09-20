package com.example.music.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.music.R
import com.example.music.databinding.FragmentArtistCrudBinding
import com.example.music.databinding.FragmentPlaylistCrudBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistCRUDFragment : Fragment() {

    private var _binding: FragmentPlaylistCrudBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPlaylistCrudBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
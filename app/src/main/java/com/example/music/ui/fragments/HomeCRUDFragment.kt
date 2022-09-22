package com.example.music.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.music.R
import com.example.music.databinding.FragmentHomeCrudBinding
import com.example.music.databinding.FragmentOnlineSongBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeCRUDFragment : Fragment() {

    private var _binding: FragmentHomeCrudBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeCrudBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.songCrudBtn.setOnClickListener {
            findNavController().navigate(R.id.songCRUDFragment)
        }
        binding.artistCrudBtn.setOnClickListener {
            findNavController().navigate(R.id.artistCRUDFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
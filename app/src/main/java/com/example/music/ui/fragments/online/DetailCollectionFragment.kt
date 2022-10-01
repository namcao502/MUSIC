package com.example.music.ui.fragments.online

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.UiState
import com.example.music.data.models.online.OnlineSong
import com.example.music.databinding.FragmentDetailCollectionBinding
import com.example.music.ui.adapters.DetailCollectionAdapter
import com.example.music.viewModels.online.FirebaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailCollectionFragment(
    val name: String,
    val songs: List<String>,
    val imgFilePath: String,
    private val clickASongInDetail: ClickASongInDetail)
    : Fragment(), DetailCollectionAdapter.ClickASong {

    private var _binding: FragmentDetailCollectionBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private val detailCollectionAdapter: DetailCollectionAdapter by lazy {
        DetailCollectionAdapter(requireContext(), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addImg.setOnClickListener {

        }

        binding.backImg.setOnClickListener {
            //back press
            requireActivity().onBackPressed()
        }

        binding.nameTv.text = name

        if (imgFilePath.isNotEmpty()){
            Glide.with(requireContext()).load(imgFilePath).into(binding.imgImg)
        }
        else {
            binding.imgImg.setImageResource(R.drawable.ic_baseline_album_24)
        }

        with(binding.listSongRv){
            adapter = detailCollectionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        firebaseViewModel.getSongFromListSongID(songs)
        firebaseViewModel.songFromID.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    detailCollectionAdapter.setData(it.data)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    interface ClickASongInDetail{
        fun callBackFromClickASongInDetail(songList: List<OnlineSong>, position: Int)
    }

    override fun callBackFromDetailClick(songList: List<OnlineSong>, position: Int) {
        clickASongInDetail.callBackFromClickASongInDetail(songList, position)
    }

}
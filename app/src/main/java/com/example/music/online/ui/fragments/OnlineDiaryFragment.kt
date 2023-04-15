package com.example.music.online.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.R
import com.example.music.databinding.FragmentOnlineDiaryBinding
import com.example.music.online.data.models.OnlineDiary
import com.example.music.online.ui.adapters.OnlineDiaryAdapter
import com.example.music.online.viewModels.OnlineDiaryViewModel
import com.example.music.utils.UiState
import com.example.music.utils.createBottomSheetDialog
import com.example.music.utils.createDialogForDeleteDiary
import com.example.music.utils.toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class OnlineDiaryFragment : Fragment(), OnlineDiaryAdapter.ClickADiary {

    private var _binding: FragmentOnlineDiaryBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onlineDiaryViewModel: OnlineDiaryViewModel by viewModels()

    private val onlineDiaryAdapter: OnlineDiaryAdapter by lazy {
        OnlineDiaryAdapter(this)
    }

    private var initialList: List<OnlineDiary>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOnlineDiaryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String): Boolean {
                searchBySubject(query.trim())
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchBySubject(newText.trim())
                return false
            }

        })

        binding.diaryRecyclerView.apply {
            adapter = onlineDiaryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        Firebase.auth.currentUser?.let {
            onlineDiaryViewModel.getAllDiaries(it)
        }

        //get all diaries
        onlineDiaryViewModel.diaries.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    onlineDiaryAdapter.setData(it.data)
                    initialList = it.data
                    if (initialList!!.isEmpty()){
                        binding.diaryRecyclerView.visibility = View.GONE
                        binding.itemMessage.visibility = View.VISIBLE
                    }
                    else {
                        binding.diaryRecyclerView.visibility = View.VISIBLE
                        binding.itemMessage.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun searchBySubject(text: String) {
        val filter: ArrayList<OnlineDiary> = ArrayList()
        for (item in initialList!!) {
            if (item.subject!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
        }
        if (text.isEmpty()){
            onlineDiaryAdapter.setData(initialList!!)
        }
        else {
            onlineDiaryAdapter.setData(filter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SimpleDateFormat")
    override fun callBackFromDiaryClick(diary: OnlineDiary) {
        //launch a new dialog for edit
        val dialog = requireActivity().createBottomSheetDialog(R.layout.diary_dialog)

        val subjectTxt = dialog.findViewById<TextView>(R.id.subject_txt)!!
        val contentTxt = dialog.findViewById<TextView>(R.id.content_txt)!!
        val songArtist = dialog.findViewById<TextView>(R.id.songArtist_txt)!!
        val dateTime = dialog.findViewById<TextView>(R.id.datetime_txt)!!
        val saveBtn = dialog.findViewById<Button>(R.id.save_btn)!!
        val cancelBtn = dialog.findViewById<Button>(R.id.cancel_btn)!!
        val deleteBtn = dialog.findViewById<Button>(R.id.delete_btn)!!

        subjectTxt.text = diary.subject
        contentTxt.text = diary.content
        songArtist.text = diary.from
        dateTime.text = diary.dateTime

        songArtist.visibility = View.VISIBLE
        dateTime.visibility = View.VISIBLE
        deleteBtn.visibility = View.VISIBLE

        cancelBtn.setOnClickListener {
            dialog.cancel()
        }

        deleteBtn.setOnClickListener {
            createDialogForDeleteDiary(diary, onlineDiaryViewModel)
            dialog.cancel()
        }

        saveBtn.setOnClickListener {
            val subject = subjectTxt.text.toString()
            val content = contentTxt.text.toString()
            val id = diary.id
            val currentDate = SimpleDateFormat("MM/dd/yyyy HH:mm").format(Date())
            val tempDiary = OnlineDiary(id, subject, content, currentDate, "", "")

            Firebase.auth.currentUser?.let {
                onlineDiaryViewModel.updateDiary(tempDiary, it)
            }
            onlineDiaryViewModel.updateDiary.observe(viewLifecycleOwner){
                when(it){
                    is UiState.Loading -> {

                    }
                    is UiState.Failure -> {

                    }
                    is UiState.Success -> {
                        toast(it.data)
                        dialog.cancel()
                    }
                }
            }
        }
        dialog.show()
    }

    override fun callBackFromDeleteDiaryClick(diary: OnlineDiary) {
        createDialogForDeleteDiary(diary, onlineDiaryViewModel)
    }

}
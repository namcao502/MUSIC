package com.example.music.online.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.R
import com.example.music.databinding.ActivitySongManagerBinding
import com.example.music.online.data.models.OnlineSong
import com.example.music.online.ui.adapters.SongManagerAdapter
import com.example.music.online.viewModels.FirebaseViewModel
import com.example.music.utils.FireStoreCollection
import com.example.music.utils.UiState
import com.example.music.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class SongManagerActivity: AppCompatActivity(), SongManagerAdapter.ClickASong {

    // val name: String, private val modelId: String, var listSong: MutableList<String>

    private lateinit var binding: ActivitySongManagerBinding

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private var allSongs: List<OnlineSong> = emptyList()
    private var currentSongs: List<OnlineSong> = emptyList()

    private val songManagerAdapter: SongManagerAdapter by lazy {
        SongManagerAdapter(this, this, firebaseViewModel)
    }

    private var name: String = ""
    private var modelId: String = ""
    private var listSong: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongManagerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

        window.navigationBarColor = resources.getColor(R.color.main_color, this.theme)
        window.statusBarColor = resources.getColor(R.color.main_color, this.theme)

        name = intent.getStringExtra(FireStoreCollection.MODEL_NAME).toString()
        modelId = intent.getStringExtra(FireStoreCollection.MODEL_ID).toString()
        listSong = intent.getStringArrayListExtra(FireStoreCollection.MODEL_SONG_LIST) as ArrayList<String>

        with(binding.thisSongsRv){
            adapter = songManagerAdapter
            layoutManager = LinearLayoutManager(this@SongManagerActivity)
        }

        //load current songs
        loadCurrent()

        binding.allSongsSv.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(text: String): Boolean {
                filterAllSongs(text)
                return false
            }

            override fun onQueryTextChange(text: String): Boolean {
                filterAllSongs(text)
                return false
            }

        })
        binding.thisSongsSv.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(text: String): Boolean {
                filterThisSongs(text)
                return false
            }

            override fun onQueryTextChange(text: String): Boolean {
                filterThisSongs(text)
                return false
            }

        })

        //load all songs
        firebaseViewModel.getAllSongs()
        firebaseViewModel.song.observe(this){
            when(it) {
                is UiState.Loading -> {

                }
                is UiState.Failure -> {

                }
                is UiState.Success -> {
                    allSongs = it.data
                    binding.allSongsLv.adapter = ArrayAdapter(this,
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, allSongs)
                }
            }
        }


        binding.allSongsLv.setOnItemClickListener { _, _, i, _ ->
            listSong.add(allSongs[i].id!!)
            update()
            loadCurrent()
        }

    }

    private fun loadCurrent(){
        //reload
        if (listSong.isNotEmpty()) {
            val songList: ArrayList<OnlineSong> = ArrayList()
            for (i in 0 until listSong.size) {
                firebaseViewModel.getSongFromSongID(listSong[i], i)
                firebaseViewModel.songFromID2[i].observe(this) {
                    when(it) {
                        is UiState.Loading -> {

                        }
                        is UiState.Failure -> {

                        }
                        is UiState.Success -> {
//                            Log.i("TAG502", "loadCurrent: ${it.data}")
                            songList.add(it.data)
                            currentSongs = songList
                            songManagerAdapter.setData(currentSongs)
                        }
                    }
                }
            }
//            toast(songList.toString())
//            currentSongs = songList
//            songManagerAdapter.setData(currentSongs)

//            firebaseViewModel.getSongFromListSongID(listSong)
//            firebaseViewModel.songFromID.observe(this){
//                when(it) {
//                    is UiState.Loading -> {
//
//                    }
//                    is UiState.Failure -> {
//
//                    }
//                    is UiState.Success -> {
//                        currentSongs = it.data
//                        toast(listSong.toString())
//                        songManagerAdapter.setData(currentSongs)
//                    }
//                }
//            }
        }
        else {
            currentSongs = listOf(OnlineSong("", "empty", "", "", ""))
            songManagerAdapter.setData(currentSongs)
        }
    }

    private fun update(){
        firebaseViewModel.updateModelById(name, modelId, listSong)
        firebaseViewModel.updateModel.observe(this){
            when(it) {
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

    private fun filterAllSongs(text: String) {
        val filter: ArrayList<OnlineSong> = ArrayList()
        for (item in allSongs) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
        }
        if (text.isEmpty()){
            toast("Not found")
            binding.allSongsLv.adapter = ArrayAdapter(this,
                androidx.appcompat.R.layout.
                support_simple_spinner_dropdown_item,
                allSongs)
        }
        else {
            binding.allSongsLv.adapter = ArrayAdapter(this,
                androidx.appcompat.R.layout.
                support_simple_spinner_dropdown_item,
                filter)
        }
    }

    private fun filterThisSongs(text: String) {
        val filter: ArrayList<OnlineSong> = ArrayList()
        for (item in currentSongs) {
            if (item.name!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filter.add(item)
            }
        }
        if (filter.isEmpty()) {
            toast("Not found")
        }
        if (text.isEmpty()){
            toast("Not found")
            songManagerAdapter.setData(currentSongs)
        }
        else {
            songManagerAdapter.setData(filter)
        }
    }

    override fun callBackFromClickASong(song: OnlineSong) {
        listSong.remove(song.id)
        update()
        loadCurrent()
    }
}
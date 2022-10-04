package com.example.music.online.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.music.R
import com.example.music.databinding.ActivityCrudBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CRUDActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrudBinding

//    private val firebaseViewModel: FirebaseViewModel by viewModels()
//
//    private lateinit  var songUri: Uri
//
//    private val PICK_AUDIO_REQUEST = 205

//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
//            songUri = data.data!!
//            try {
//                val inputStream = contentResolver.openInputStream(data.data!!)
//                Log.i("TAG502", "onActivityResult: $songUri , $inputStream")
//            } catch (e: FileNotFoundException) {
//                e.printStackTrace()
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrudBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupActionBarWithNavController(findNavController(R.id.fragmentContainerView_crud))

//        binding.textView2.setOnClickListener {
//            val intent = Intent()
//            intent.type = "Songs/"
//            intent.action = Intent.ACTION_GET_CONTENT
//            startActivityForResult(Intent.createChooser(intent, "Select Song"), PICK_AUDIO_REQUEST)
//        }
//
//        binding.button.setOnClickListener {
//            val fileName = "SunGOD"
//            firebaseViewModel.uploadSingleSongFile(fileName, songUri){
//                when (it) {
//                    is UiState.Loading -> {
//
//                    }
//                    is UiState.Failure -> {
//
//                    }
//                    is UiState.Success -> {
//                        Toast.makeText(this, it.data.toString(), Toast.LENGTH_SHORT).show()
//                        //pass here
//                        //create song in firebase here and add it
//                    }
//            }
//        }
//
//        var artist: ArrayList<OnlineArtist> = arrayListOf(
//            OnlineArtist("", "Warriyo", emptyList(), ""),
//            OnlineArtist("", "Laura Brehm", emptyList(), ""),
//            OnlineArtist("", "Different Heaven", emptyList(), ""),
//            OnlineArtist("", "Culture Code", emptyList(), ""),
//            OnlineArtist("", "Karra", emptyList(), ""),
//            OnlineArtist("", "Elektronomia", emptyList(), ""),
//            OnlineArtist("", "Heuse", emptyList(), ""),
//            OnlineArtist("", "Zeus x Crona", emptyList(), ""),
//            OnlineArtist("", "Emma Sameth", emptyList(), ""),
//            OnlineArtist("", "Unknown Brain", emptyList(), ""),
//            OnlineArtist("", "Ship Wrek", emptyList(), ""),
//            OnlineArtist("", "Mia Vaile", emptyList(), ""),
//            OnlineArtist("", "Tobu", emptyList(), ""),
//            OnlineArtist("", "RetroVision", emptyList(), ""),
//            OnlineArtist("", "Syn Cole", emptyList(), ""),
//            OnlineArtist("", "Cartoon", emptyList(), ""),
//            OnlineArtist("", "Daniel Levi", emptyList(), ""),
//            OnlineArtist("", "Jim Yosef", emptyList(), ""),
//            OnlineArtist("", "Janji", emptyList(), ""),
//            OnlineArtist("", "Johnning", emptyList(), ""),
//            OnlineArtist("", "DEAF KEV", emptyList(), ""),
//            OnlineArtist("", "SirensCeol", emptyList(), ""),
//            OnlineArtist("", "Itro", emptyList(), ""),
//            OnlineArtist("", "Spektrem", emptyList(), ""),
//            OnlineArtist("", "EH!DE", emptyList(), ""),
//            OnlineArtist("", "Veela", emptyList(), ""),
//            OnlineArtist("", "ElectroLight", emptyList(), ""),
//            OnlineArtist("", "Tetrix Bass", emptyList(), ""),
//        )
//
////        for (x in artist){
////            firebaseViewModel.addArtist(x)
////            firebaseViewModel.artists.observe(this){
////                when (it) {
////                    is UiState.Loading -> {
////
////                    }
////                    is UiState.Failure -> {
////
////                    }
////                    is UiState.Success -> {
////                        Toast.makeText(this, it.data, Toast.LENGTH_SHORT).show()
////                    }
////                }
////            }
////        }
//
//        var songs: ArrayList<OnlineSong> = arrayListOf(
//            OnlineSong("", "Mortals", "", ""),
//            OnlineSong("", "Safe And Sound", "", ""),
//            OnlineSong("", "Make Me Move", "", ""),
//            OnlineSong("", "Sky High", "", ""),
//            OnlineSong("", "Pill", "", ""),
//            OnlineSong("", "Superhero", "", ""),
//            OnlineSong("", "Pain", "", ""),
//            OnlineSong("", "Energy", "", ""),
//            OnlineSong("", "Roots", "", ""),
//            OnlineSong("", "Puzzle", "", ""),
//            OnlineSong("", "Feel Good", "", ""),
//            OnlineSong("", "On & On", "", ""),
//            OnlineSong("", "Firefly", "", ""),
//            OnlineSong("", "Heroes Tonight", "", ""),
//            OnlineSong("", "Invincible", "", ""),
//            OnlineSong("", "Candyland", "", ""),
//            OnlineSong("", "Nostalgia", "", ""),
//            OnlineSong("", "Life", "", ""),
//            OnlineSong("", "Sunburst", "", ""),
//            OnlineSong("", "Shine(Gabriel Drew & Bloom Remix)", "", ""),
//            OnlineSong("", "My Heart", "", ""),
//            OnlineSong("", "The Light ", "", ""),
//            OnlineSong("", "Symbolism", "", ""),
//        )
//
////        for (x in songs){
////            firebaseViewModel.addSong(x)
////            firebaseViewModel.addSong.observe(this){
////                when (it) {
////                    is UiState.Loading -> {
////
////                    }
////                    is UiState.Failure -> {
////
////                    }
////                    is UiState.Success -> {
////                        Toast.makeText(this, it.data, Toast.LENGTH_SHORT).show()
////                    }
////                }
////            }
//        }
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragmentContainerView_crud)
        return super.onSupportNavigateUp() || navController.navigateUp()
    }
}
package com.example.music.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.music.R
import com.example.music.databinding.ActivityMainBinding
import com.example.music.ui.fragments.PlaylistFragment
import com.example.music.ui.fragments.SongFragment
import com.example.music.ui.fragments.ViewPagerAdapter
import com.example.music.viewModels.ScanSongInStorage
import com.example.music.viewModels.SongViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val songViewModel: SongViewModel by viewModels()

    private val permission = 502

    private lateinit var binding: ActivityMainBinding

    private var songFragment: SongFragment = SongFragment()
    private var playlistFragment: PlaylistFragment = PlaylistFragment()
    private  var fragmentList: MutableList<Fragment> = mutableListOf(songFragment, playlistFragment)

    private lateinit var viewPagerChart: ViewPagerAdapter

    private val tabLayoutTitles:ArrayList<String> = arrayListOf("Song","Playlist")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //SET ADAPTER
        viewPagerChart = ViewPagerAdapter(supportFragmentManager, lifecycle, fragmentList)
        binding.viewPagerMain.adapter = viewPagerChart

        //SET TAB TITLE AND MAP WITH FRAGMENT
        TabLayoutMediator(binding.tabLayoutMain, binding.viewPagerMain) { tab, position ->
            tab.text = tabLayoutTitles[position]
        }.attach()

        requestRead()

    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater: MenuInflater = menuInflater
//        inflater.inflate(R.menu.main_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            R.id.scan_menu -> requestRead()
//        }
//        return false
//    }

    fun requestRead() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                permission)
        } else {
            readFile()
        }
    }

    private fun readFile(){
        val context = this.applicationContext
        val listSong = ScanSongInStorage(context).getAllSongs()
        songViewModel.deleteAllSongs()
        for (song in listSong){
            songViewModel.addSong(song)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == permission) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readFile()
            } else {
                // Permission Denied
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
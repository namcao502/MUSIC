package com.example.music.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.fragment.app.Fragment
import com.example.music.ui.fragments.PlaylistFragment
import com.example.music.R
import com.example.music.ui.fragments.SongFragment
import com.example.music.ui.fragments.ViewPagerAdapter
import com.example.music.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

}
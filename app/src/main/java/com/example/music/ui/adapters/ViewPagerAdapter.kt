package com.example.music.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentManager: FragmentManager, lifeCycle: Lifecycle, fragmentsTemp:MutableList<Fragment>)
    : FragmentStateAdapter(fragmentManager,lifeCycle) {


    private var fragments: MutableList<Fragment> = fragmentsTemp

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
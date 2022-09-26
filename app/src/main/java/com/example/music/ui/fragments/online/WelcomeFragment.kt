package com.example.music.ui.fragments.online

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.music.R
import com.example.music.databinding.FragmentHomeBinding
import com.example.music.databinding.FragmentWelcomeBinding
import com.example.music.ui.activities.CRUDActivity
import com.example.music.ui.activities.MainActivity
import com.example.music.ui.activities.OnlineMainActivity
import com.example.music.viewModels.online.FirebaseAuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WelcomeFragment : Fragment() {

    private var _binding : FragmentWelcomeBinding? = null
    private val binding get() = _binding
    private val firebaseAuthViewModel: FirebaseAuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        getUser()
        registerObserver()
        listenToChannels()

        binding?.offlineButton?.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        binding?.adminBtn?.setOnClickListener {
            startActivity(Intent(requireContext(), CRUDActivity::class.java))
        }

        return binding?.root
    }

    private fun getUser() {
        firebaseAuthViewModel.getCurrentUser()
    }

    private fun listenToChannels() {
        viewLifecycleOwner.lifecycleScope.launch {
           firebaseAuthViewModel.allEventsFlow.collect { event ->
               when(event){
                   is FirebaseAuthViewModel.AllEvents.Message ->{
                       Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                   }
                   else -> {}
               }
           }
        }
    }

    private fun registerObserver() {
        firebaseAuthViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding?.apply {
                    //login success
                    welcomeTxt.text = "Welcome ${it.email}"
                    signinButton.text = "Sign out"
                    signinButton.setOnClickListener {
                        firebaseAuthViewModel.signOut()
                    }
                    onlineButton.visibility = View.VISIBLE
                    onlineButton.setOnClickListener {
                        startActivity(Intent(requireContext(), OnlineMainActivity::class.java))
                    }
                    startActivity(Intent(requireContext(), OnlineMainActivity::class.java))
                }
            } ?: binding?.apply {
                welcomeTxt.isVisible = false
                signinButton.text = "Sign in for online"
                signinButton.setOnClickListener {
                    findNavController().navigate(R.id.action_homeFragment_to_signInFragment)
                }
                onlineButton.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
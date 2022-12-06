package com.example.music.online.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.music.R
import com.example.music.databinding.FragmentSigninBinding
import com.example.music.online.ui.activities.OnlineMainActivity
import com.example.music.online.viewModels.FirebaseAuthViewModel
import com.example.music.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment: Fragment() {

    private val firebaseAuthViewModel: FirebaseAuthViewModel by activityViewModels()
    private var _binding: FragmentSigninBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentSigninBinding.inflate(inflater , container , false)

        if ((activity as AppCompatActivity?)!!.supportActionBar != null) {
            (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        }

        getUser()

        listenToChannels()
        registerObservers()

        binding.apply {

            signInButton.setOnClickListener {

                progressBarSignin.isVisible = true
                val email = userEmailEtv.text.toString()
                val password = userPasswordEtv.text.toString()
                firebaseAuthViewModel.signInUser(email, password)
            }

            signUpTxt.setOnClickListener {
                findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
            }

            forgotPassTxt.setOnClickListener {
                findNavController().navigate(R.id.action_signInFragment_to_resetPasswordFragment)
            }
        }
        return binding.root
    }

    private fun registerObservers() {
        firebaseAuthViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                startActivity(Intent(requireContext(), OnlineMainActivity::class.java))
            }
        }
    }

    private fun listenToChannels() {
        viewLifecycleOwner.lifecycleScope.launch {
            firebaseAuthViewModel.allEventsFlow.collect { event ->
                when(event){
                    is FirebaseAuthViewModel.AllEvents.Error -> {
                        binding.apply {
                            errorTxt.text =  event.error
                            progressBarSignin.visibility = View.GONE
                        }
                    }
                    is FirebaseAuthViewModel.AllEvents.Message -> {
                        toast(event.message)
                    }
                    is FirebaseAuthViewModel.AllEvents.ErrorCode -> {
                        if (event.code == 1)
                            binding.apply {
                                userEmailEtvl.error = "Email should not be empty"
                                progressBarSignin.visibility = View.GONE
                            }
                        if(event.code == 2)
                            binding.apply {
                                userPasswordEtvl.error = "Password should not be empty"
                                progressBarSignin.visibility = View.GONE
                            }
                    }
                }
            }
        }
    }

    private fun getUser() {
        firebaseAuthViewModel.getCurrentUser()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
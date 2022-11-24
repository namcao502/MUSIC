package com.example.music.online.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.music.R
import com.example.music.databinding.FragmentSignupBinding
import com.example.music.online.ui.activities.OnlineMainActivity
import com.example.music.online.viewModels.AuthenticationViewModel
import com.example.music.online.viewModels.FirebaseAuthViewModel
import com.example.music.online.viewModels.OnlineAccountViewModel
import com.example.music.utils.UiState
import com.example.music.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val firebaseAuthViewModel: FirebaseAuthViewModel by activityViewModels()
//    private val authViewModel: AuthenticationViewModel by viewModels()
    private var _binding: FragmentSignupBinding? = null
    private val binding get()  = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater , container , false)

        getUser()
//        listenToChannels2()
        registerObserver2()

        registerObservers()
        listenToChannels()

        binding.apply {
            signUpButton.setOnClickListener {
                progressBarSignup.isVisible = true
                val email = userEmailEtv.text.toString()
                val password = userPasswordEtv.text.toString()
                val confirmPass = confirmPasswordEtv.text.toString()

//                if (email.isEmpty() || password.isEmpty()){
//                    toast("Please fill them all!!!")
//                    return@setOnClickListener
//                }
//
//                if (password != confirmPass){
//                    toast("Confirm password is wrong!!!")
//                    return@setOnClickListener
//                }

//                authViewModel.signUpWithEmailPassword(email, password)
//                authViewModel.signUp.observe(viewLifecycleOwner){
//                    when (it) {
//                        is UiState.Loading -> {
//
//                        }
//                        is UiState.Failure -> {
//                            toast(it.toString())
//                        }
//                        is UiState.Success -> {
//                            toast(it.data)
//                            progressBarSignup.isVisible = false
//
//                            toast("Signing you in...")
//                            authViewModel.signInWithEmailPassword(email, password)
//                            authViewModel.signIn.observe(viewLifecycleOwner){ signIn ->
//                                when (signIn) {
//                                    is UiState.Loading -> {
//
//                                    }
//                                    is UiState.Failure -> {
//                                        toast(signIn.toString())
//                                    }
//                                    is UiState.Success -> {
//                                        toast("Welcome back!!!")
//                                        startActivity(Intent(requireContext(), OnlineMainActivity::class.java))
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
                firebaseAuthViewModel.signUpUser(email , password , confirmPass)
            }

            signInTxt.setOnClickListener {
                findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
            }

        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun registerObservers() {
        firebaseAuthViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
//                findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)
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
                            errorTxt.text = event.error
                            progressBarSignup.isInvisible = true
                        }
                    }
                    is FirebaseAuthViewModel.AllEvents.Message -> {
                        toast(event.message)
                    }
                    is FirebaseAuthViewModel.AllEvents.ErrorCode -> {
                        if (event.code == 1)
                            binding.apply {
                                userEmailEtvl.error = "Email should not be empty"
                                progressBarSignup.isInvisible = true
                            }

                        if(event.code == 2)
                            binding.apply {
                                userPasswordEtvl.error = "Password should not be empty"
                                progressBarSignup.isInvisible = true
                            }

                        if(event.code == 3)
                            binding.apply {
                                confirmPasswordEtvl.error = "Passwords do not match"
                                progressBarSignup.isInvisible = true
                            }
                    }
                }

            }
        }
    }
    private fun getUser() {
        firebaseAuthViewModel.getCurrentUser()
    }

    private fun listenToChannels2() {
        viewLifecycleOwner.lifecycleScope.launch {
            firebaseAuthViewModel.allEventsFlow.collect { event ->
                when(event){
                    is FirebaseAuthViewModel.AllEvents.Message ->{
                        toast(event.message)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun registerObserver2() {
        firebaseAuthViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                startActivity(Intent(requireContext(), OnlineMainActivity::class.java))
            }
        }
    }
}
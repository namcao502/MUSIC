package com.example.music.ui.fragments.online

import android.accounts.Account
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
import com.example.music.viewModels.online.FirebaseAuthViewModel
import com.example.music.viewModels.online.OnlineAccountViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private val viewModel: FirebaseAuthViewModel by activityViewModels()
    private var _binding: FragmentSignupBinding? = null
    private val onlineAccountViewModel: OnlineAccountViewModel by viewModels()
    private val binding get()  = _binding
    private val TAG = "SignUpFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater , container , false)

        registerObservers()
        listenToChannels()

        binding?.apply {
            signUpButton.setOnClickListener {
                progressBarSignup.isVisible = true
                val email = userEmailEtv.text.toString()
                val password = userPasswordEtv.text.toString()
                val confirmPass = confirmPasswordEtv.text.toString()
                viewModel.signUpUser(email , password , confirmPass)
            }

            signInTxt.setOnClickListener {
                findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
            }

        }

        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun registerObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)
            }
        }
    }

    private fun listenToChannels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allEventsFlow.collect { event ->
                when(event){
                    is FirebaseAuthViewModel.AllEvents.Error -> {
                        binding?.apply {
                            errorTxt.text = event.error
                            progressBarSignup.isInvisible = true
                        }
                    }
                    is FirebaseAuthViewModel.AllEvents.Message -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                    is FirebaseAuthViewModel.AllEvents.ErrorCode -> {
                        if (event.code == 1)
                            binding?.apply {
                                userEmailEtvl.error = "email should not be empty"
                                progressBarSignup.isInvisible = true
                            }


                        if(event.code == 2)
                            binding?.apply {
                                userPasswordEtvl.error = "password should not be empty"
                                progressBarSignup.isInvisible = true
                            }

                        if(event.code == 3)
                            binding?.apply {
                                confirmPasswordEtvl.error = "passwords do not match"
                                progressBarSignup.isInvisible = true
                            }
                    }

                    else ->{
                        Log.d(TAG, "listenToChannels: No event received so far")
                    }
                }

            }
        }
    }
}
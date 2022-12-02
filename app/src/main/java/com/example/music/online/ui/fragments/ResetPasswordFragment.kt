package com.example.music.online.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.music.R
import com.example.music.databinding.FragmentPasswordResetBinding
import com.example.music.online.viewModels.FirebaseAuthViewModel
import com.example.music.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {

    private var _binding : FragmentPasswordResetBinding? = null
    private val binding get() = _binding!!
    private val viewModel : FirebaseAuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPasswordResetBinding.inflate(inflater , container , false)

        setUpWidgets()
        listenToChannels()

        binding.apply {
            buttonResendPassword.setOnClickListener {
                resetPassProgressBar.isVisible = true
                val email = userEmailEtv.text.toString()
                viewModel.verifySendPasswordReset(email)
            }
        }

        return binding.root
    }

    private fun listenToChannels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allEventsFlow.collect { event ->
                when(event){
                    is FirebaseAuthViewModel.AllEvents.Message -> {
                        toast(event.message)
                        findNavController().navigate(R.id.action_resetPasswordFragment_to_signInFragment)
                    }
                    is FirebaseAuthViewModel.AllEvents.Error -> {
                        binding.apply {
                            resetPassProgressBar.isInvisible = true
                            errorText.text = event.error
                        }
                    }
                    is FirebaseAuthViewModel.AllEvents.ErrorCode -> {
                        if(event.code == 1)
                            binding.apply {
                                userEmailEtvl.error = "email should not be empty!"
                                resetPassProgressBar.isInvisible = true
                            }
                    }
                }

            }
        }
    }

    private fun setUpWidgets() {
        binding.apply {
            buttonResendPassword.setOnClickListener {
                resetPassProgressBar.isVisible = true
                val email = userEmailEtv.text.toString()
                viewModel.verifySendPasswordReset(email)
            }
        }
    }
}
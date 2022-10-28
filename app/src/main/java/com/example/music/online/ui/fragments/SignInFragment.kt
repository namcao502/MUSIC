package com.example.music.online.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.music.R
import com.example.music.databinding.FragmentSigninBinding
import com.example.music.online.ui.activities.OnlineMainActivity
import com.example.music.online.viewModels.FirebaseAuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment: Fragment() {

    private val viewModel : FirebaseAuthViewModel by activityViewModels()
    private val firebaseAuthViewModel: FirebaseAuthViewModel by activityViewModels()
    private var _binding: FragmentSigninBinding? = null
    private val binding get() = _binding!!
    private lateinit var rememberSP: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSigninBinding.inflate(inflater , container , false)


        if ((activity as AppCompatActivity?)!!.supportActionBar != null) {
            (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        }

        getUser()
        listenToChannels2()
        registerObserver2()

        listenToChannels()
        registerObservers()

        binding.apply {

            signInButton.setOnClickListener {
                progressBarSignin.isVisible = true
                val email = userEmailEtv.text.toString()
                val password = userPasswordEtv.text.toString()
                viewModel.signInUser(email, password)
            }

            signUpTxt.setOnClickListener {
                findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
            }

            forgotPassTxt.setOnClickListener {
                findNavController().navigate(R.id.action_signInFragment_to_resetPasswordFragment)
            }

            rememberSP = requireActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE)
            userEmailEtv.setText(rememberSP.getString("email", ""))
            userPasswordEtv.setText(rememberSP.getString("password", ""))
            rememberCb.isChecked = rememberSP.getBoolean("check", false)
        }

        return binding.root
    }

    private fun registerObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
//                findNavController().navigate(R.id.action_signInFragment_to_homeFragment2)
                startActivity(Intent(requireContext(), OnlineMainActivity::class.java))
            }
        }
    }

    private fun listenToChannels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allEventsFlow.collect { event ->
                when(event){
                    is FirebaseAuthViewModel.AllEvents.Error -> {
                        binding.apply {
                            errorTxt.text =  event.error
                            progressBarSignin.isInvisible = true
                        }
                    }
                    is FirebaseAuthViewModel.AllEvents.Message -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                        //sign in success
                        val editor = rememberSP.edit()
                        if (binding.rememberCb.isChecked){
                            editor.putString("email", binding.userEmailEtv.text.toString())
                            editor.putString("password", binding.userPasswordEtv.text.toString())
                            editor.putBoolean("check", true)
                            editor.apply()
                        }
                        else {
                            editor.putString("email", "")
                            editor.putString("password", "")
                            editor.putBoolean("check", false)
                            editor.apply()
                        }
                    }
                    is FirebaseAuthViewModel.AllEvents.ErrorCode -> {
                        if (event.code == 1)
                            binding.apply {
                                userEmailEtvl.error = "email should not be empty"
                                progressBarSignin.isInvisible = true
                            }


                        if(event.code == 2)
                            binding.apply {
                                userPasswordEtvl.error = "password should not be empty"
                                progressBarSignin.isInvisible = true
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
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
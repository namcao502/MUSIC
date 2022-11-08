package com.example.music.online.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.music.utils.toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment: Fragment() {

    private val viewModel: FirebaseAuthViewModel by activityViewModels()
    private val firebaseAuthViewModel: FirebaseAuthViewModel by activityViewModels()
//    private val authViewModel: AuthenticationViewModel by viewModels()
    private var _binding: FragmentSigninBinding? = null
    private val binding get() = _binding!!
    private lateinit var rememberSP: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentSigninBinding.inflate(inflater , container , false)

        if ((activity as AppCompatActivity?)!!.supportActionBar != null) {
            (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        }

        getUser()
        listenToChannels2()

        listenToChannels()
        registerObservers()

        binding.apply {

            rememberSP = requireActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE)
            userEmailEtv.setText(rememberSP.getString("email", ""))
            userPasswordEtv.setText(rememberSP.getString("password", ""))
            val stillSignIn = rememberSP.getBoolean("check", false)
            rememberCb.isChecked = stillSignIn

//            if (stillSignIn){
//                toast("Signing you in...")
//                authViewModel.signInWithEmailPassword(rememberSP.getString("email", "")!!, rememberSP.getString("password", "")!!)
//                authViewModel.signIn.observe(viewLifecycleOwner){
//                    when (it) {
//                        is UiState.Loading -> {
//
//                        }
//                        is UiState.Failure -> {
//                            toast(it.toString())
//                        }
//                        is UiState.Success -> {
//                            toast("Welcome back!!!")
//                            startActivity(Intent(requireContext(), OnlineMainActivity::class.java))
//                        }
//                    }
//                }
//            }

            signInButton.setOnClickListener {

                progressBarSignin.isVisible = true
                val email = userEmailEtv.text.toString()
                val password = userPasswordEtv.text.toString()

//                if (email.isEmpty() || password.isEmpty()){
//                    toast("Please fill them all!!!")
//                    return@setOnClickListener
//                }

//                authViewModel.signInWithEmailPassword(email, password)
//                authViewModel.signIn.observe(viewLifecycleOwner){
//                    when (it) {
//                        is UiState.Loading -> {
//
//                        }
//                        is UiState.Failure -> {
//                            toast(it.toString())
//                        }
//                        is UiState.Success -> {
//                            val editor = rememberSP.edit()
//                            if (binding.rememberCb.isChecked){
//                                editor.putString("email", binding.userEmailEtv.text.toString())
//                                editor.putString("password", binding.userPasswordEtv.text.toString())
//                                editor.putBoolean("check", true)
//                                editor.apply()
//                            }
//                            else {
//                                editor.putString("email", "")
//                                editor.putString("password", "")
//                                editor.putBoolean("check", false)
//                                editor.apply()
//                            }
//                            toast(it.data)
//                            progressBarSignin.isVisible = false
//                            startActivity(Intent(requireContext(), OnlineMainActivity::class.java))
//                        }
//                    }
//                }
                viewModel.signInUser(email, password)
                if (Firebase.auth.currentUser != null){
                    startActivity(Intent(requireContext(), OnlineMainActivity::class.java))
                }
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
                        toast(event.message)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
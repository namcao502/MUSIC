package com.example.music.online.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.music.R
import com.example.music.databinding.FragmentSigninBinding
import com.example.music.online.data.models.OnlineAccount
import com.example.music.online.ui.activities.OnlineMainActivity
import com.example.music.online.viewModels.FirebaseAuthViewModel
import com.example.music.utils.FireStoreCollection
import com.example.music.utils.toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileNotFoundException


@AndroidEntryPoint
class SignInFragment: Fragment() {

    private val firebaseAuthViewModel: FirebaseAuthViewModel by activityViewModels()
    private var _binding: FragmentSigninBinding? = null
    private val binding get() = _binding!!

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var rememberID: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentSigninBinding.inflate(inflater , container , false)

        if ((activity as AppCompatActivity?)!!.supportActionBar != null) {
            (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        }

        rememberID = requireActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE)

        getUser()

        listenToChannels()
        registerObservers()

        FirebaseApp.initializeApp(requireContext())

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        firebaseAuth = FirebaseAuth.getInstance()

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

            btnGgSignIn.setOnClickListener{
                signInWithGoogle()
            }

        }
        return binding.root
    }

    private fun signInWithGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        activityResult.launch(signInIntent)
    }

    private val activityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                updateUI(account)
            }
        } catch (e: ApiException) {
            toast("${e.statusCode}")
        }
    }

    // this is where we update the UI after Google signin takes place
    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //create account here
                if (Firebase.auth.currentUser?.uid.equals(rememberID.getString("ID", ""))){
                    startActivity(Intent(requireContext(), OnlineMainActivity::class.java))
                }
                else {
                    val doc = FirebaseFirestore.getInstance().collection(FireStoreCollection.ACCOUNT).document()
                    val user = OnlineAccount(doc.id, Firebase.auth.currentUser?.uid,"", "", "", "", "")
                    doc.set(user)
                        .addOnSuccessListener {
                            val editor = rememberID.edit()
                            editor.putString("ID", user.userID)
                            editor.apply()
                            startActivity(Intent(requireContext(), OnlineMainActivity::class.java))
                        }
                        .addOnFailureListener {
                            toast("Please try again...")
                        }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(requireContext()) != null) {
            startActivity(Intent(requireContext(), OnlineMainActivity::class.java))
        }
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
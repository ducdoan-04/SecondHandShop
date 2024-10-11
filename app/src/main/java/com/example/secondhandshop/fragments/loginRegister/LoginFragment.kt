package com.example.secondhandshop.fragments.loginRegister

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.secondhandshop.R
import com.example.secondhandshop.activities.ShoppingActivity
import com.example.secondhandshop.data.User
import com.example.secondhandshop.databinding.FragmentLoginBinding
import com.example.secondhandshop.dialog.setupBottomSheetDialog
import com.example.secondhandshop.util.Constants
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<LoginViewModel>()

    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore
    private val  _register = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val register: Flow<Resource<User>> = _register

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            manageResult(task)
        }
    }

    private fun manageResult(task: Task<GoogleSignInAccount>) {
        val account : GoogleSignInAccount? = task.result
        if (account != null ) {
            // Lấy thông tin từ đối tượng GoogleSignInAccount
            val email = account.email
            val firstName = account.givenName ?: "."
            val lastName = account.familyName ?: "."


//          val imagePath = account.photoUrl?.toString() ?: ""
            val imagePath = if (account.photoUrl == null) {
                "https://firebasestorage.googleapis.com/v0/b/secondhandshop-2f45f.appspot.com/o/profileImages%2Flogo-01.png?alt=media&token=3a71279e-eb87-4c52-b1fd-7125311a7611"
            } else {
                account.photoUrl.toString()
            }

            val displayName = account.displayName


            // Tạo một đối tượng User từ thông tin của tài khoản Google
            val user = User(firstName, lastName, email ?: "", imagePath,"1","0")
            saveUserInfo(account.id ?: "", user)


            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val intent = Intent(requireActivity(), ShoppingActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(requireContext(), "Account created", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), authTask.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
        }else{
            Toast.makeText(requireContext(), task.exception.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun saveUserInfo(userUid: String, user: User) {
        db.collection(Constants.USER_COLLECTION)
            .document(userUid)
            .set(user)
            .addOnSuccessListener {
                _register.value = Resource.Success(user)

            }.addOnFailureListener {
                _register.value = Resource.Error(it.message.toString())
            }
    }

    private fun googleSiginIn() {
        val signInClient = googleSignInClient.signInIntent
        launcher.launch(signInClient)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //----

        // Initialize db here
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance();

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient =GoogleSignIn.getClient(requireActivity(),gso)

        binding.googleLogin.setOnClickListener {
            googleSiginIn();
        }

        //---


        binding.tvDontHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.apply {
            buttonLoginLogin.setOnClickListener {
                val email = edEmailLogin.text.toString().trim()
                val password = edPasswordLogin.text.toString()
                viewModel.login(email, password)
            }
        }

        binding.tvForgotPasswordLogin.setOnClickListener {
            setupBottomSheetDialog {email ->
                viewModel.resetPassword(email)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.resetPassword.collect{
                when(it){
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        Snackbar.make(requireView(), "Reset link was sent to your email", Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Error -> {
                        Snackbar.make(requireView(), "Error: ${it.message}", Snackbar.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.login.collect{
                when(it){
                    is Resource.Loading -> {
                        binding.buttonLoginLogin.startAnimation()
                    }
                    is Resource.Success -> {
                        binding.buttonLoginLogin.revertAnimation()
                        Intent(requireActivity(),ShoppingActivity::class.java).also { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        binding.buttonLoginLogin.revertAnimation()

                    }
                    else -> Unit
                }
            }
        }
    }


}
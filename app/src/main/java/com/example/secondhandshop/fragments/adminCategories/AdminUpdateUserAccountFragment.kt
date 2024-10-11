package com.example.secondhandshop.fragments.adminCategories

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.secondhandshop.R
import com.example.secondhandshop.data.Accounts
import com.example.secondhandshop.databinding.FragmentAdminUpdateUserAccountBinding
import com.example.secondhandshop.dialog.setupBottomSheetDialog
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.viewmodel.admin.AdminUpdateUserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AdminUpdateUserAccountFragment: Fragment(R.layout.fragment_admin_update_user_account) {
    private lateinit var binding: FragmentAdminUpdateUserAccountBinding
    private val viewModel by viewModels<AdminUpdateUserAccountViewModel>()
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>
    private val status = arrayOf("Unban","Ban")
    private val positions = arrayOf("Admin", "User")

    private var imageUri : Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            imageUri = it.data?.data
            Glide.with(this).load(imageUri).into(binding.imageUser)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminUpdateUserAccountBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = arguments?.let { AdminUpdateUserAccountFragmentArgs.fromBundle(it).email }
        email?.let {
            viewModel.getUserAccount(it)
//            viewModel.getUserUID(it)
//                .addOnSuccessListener { uid ->
//                    binding.edFirstName.setText("$uid")
//                } .addOnFailureListener { exception ->
//                    binding.edFirstName.setText("$it")
//                }
//            binding.edFirstName.setText(otherUserUid)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.userAccount.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showUserLoading()
                    }
                    is Resource.Success -> {
                        hideUserLoading()
                        showUserInformation(it.data!!)
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.updateUserAccount.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.buttonSave.startAnimation()
                    }
                    is Resource.Success -> {
                        val mail = binding.edEmail.text.toString().trim()
                        binding.buttonSave.revertAnimation()
                        Toast.makeText(requireContext(), "Update account '${mail}' success" , Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        binding.tvUpdatePassword.setOnClickListener{
            setupBottomSheetDialog {

            }
        }

        binding.buttonSave.setOnClickListener {
            binding.apply {
                val firstName = edFirstName.text.toString().trim()
                val lastName  = edLastName.text.toString().trim()
                val email = edEmail.text.toString().trim()

                val selectedStatus = spinnerStatus.selectedItem.toString()
                val status = if (selectedStatus == "Unban") "1" else "0"

                val selectedPosition = spinnerPosition.selectedItem.toString()
                val position = if (selectedPosition == "Admin") "1" else "0"

                val account = Accounts(firstName,lastName,email,"",status,position)
                viewModel.updateUser(account,imageUri , email)
            }
        }

        binding.imageEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imageActivityResultLauncher.launch(intent)
        }


    }



    private fun showUserInformation(data: Accounts) {
        binding.apply {
            Glide.with(this@AdminUpdateUserAccountFragment).load(data.imagePath).error(ColorDrawable(Color.BLACK)).into(imageUser)
            edFirstName.setText(data.firstName)
            edLastName.setText(data.lastName)
            edEmail.setText(data.email)

//            val defaultSatus = "${data?.status}"
//            val statusArray = resources.getStringArray(R.array.user_account_status)
//            val defaultIndex = statusArray.indexOf(defaultSatus)

//            binding.spinnerStatus.setSelection(defaultIndex)
            // Position == 1 is Admin else User
            // Status == 1 is Unban else Ban
            spinnerStatus.setSelection(if (data.status == "1") 0 else 1)
            spinnerPosition.setSelection(if (data.position == "1") 0 else 1)
        }
    }

    private fun hideUserLoading() {
        binding.apply {
            progressbarAccount.visibility = View.GONE
            imageUser.visibility = View.VISIBLE
            imageEdit.visibility = View.VISIBLE
            edFirstName.visibility = View.VISIBLE
            edLastName.visibility = View.VISIBLE
            edEmail.visibility = View.VISIBLE
            spinnerStatus.visibility = View.VISIBLE
            spinnerPosition.visibility = View.VISIBLE
            tvUpdatePassword.visibility = View.VISIBLE
            buttonSave.visibility = View.VISIBLE
        }

    }

    private fun showUserLoading() {
        binding.apply {
            progressbarAccount.visibility = View.VISIBLE
            imageUser.visibility = View.INVISIBLE
            imageEdit.visibility = View.INVISIBLE
            edFirstName.visibility = View.INVISIBLE
            edLastName.visibility = View.INVISIBLE
            edEmail.visibility = View.INVISIBLE
            spinnerStatus.visibility = View.INVISIBLE
            spinnerPosition.visibility = View.INVISIBLE
            tvUpdatePassword.visibility = View.INVISIBLE
            buttonSave.visibility = View.INVISIBLE
        }

    }
}
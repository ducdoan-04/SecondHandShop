package com.example.secondhandshop.fragments.adminCategories

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.secondhandshop.R
import com.example.secondhandshop.data.CategoryAdmin
import com.example.secondhandshop.data.Report
import com.example.secondhandshop.data.User
import com.example.secondhandshop.fragments.shopping.AdminHomeFragment
import com.example.secondhandshop.fragments.shopping.AdminHomeFragmentDirections
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.viewmodel.admin.CategoryAdminManageAccountsViewModel
import com.example.secondhandshop.viewmodel.factory.BaseCategoryAdminViewModelFactoryFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject


@AndroidEntryPoint
class ManageAccountsFragment: AdminBaseCategoryFragment() {

    @Inject
    lateinit var firestore: FirebaseFirestore
    lateinit var firebaseAuth: FirebaseAuth
    val viewModel by viewModels<CategoryAdminManageAccountsViewModel>{
        BaseCategoryAdminViewModelFactoryFactory(firestore,firebaseAuth, CategoryAdmin.ManageAccounts)
    }
    private val categories = arrayOf("Admin", "Client")

//    private val viewModel: CategoryAdminManageAccountsViewModel by viewModels {
//        BaseCategoryAdminViewModelFactoryFactory(firestore, CategoryAdmin.ManageAccounts)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        lifecycleScope.launchWhenStarted {
            viewModel.manageAccounts.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showManageAccountLoading()
                    }
                    is Resource.Success -> {
                        manageaccountsAdapter.differ.submitList(it.data)
                        hideManageAccountLoading()
                    }
                    is Resource.Error -> {
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                        hideManageAccountLoading()
                    }
                    else -> Unit
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.verifyacccount.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showManageAccountLoading()
                    }
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), "Update status success", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                        findNavController().navigate(R.id.adminHomeFragment)
                        hideManageAccountLoading()
                    }
                    is Resource.Error -> {
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                        hideManageAccountLoading()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.deleteAccount.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showManageAccountLoading()
                    }
                    is Resource.Success -> {
                        findNavController().popBackStack()
                        hideManageAccountLoading()
                    }
                    is Resource.Error -> {
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                        hideManageAccountLoading()
                    }
                    else -> Unit
                }
            }
        }


        binding.linearAddAccount.setOnClickListener {
            showCreateAccountDialog()
        }
        manageaccountsAdapter.onVerifiAccountClick={email->
            if (email.isEmpty()){
                Toast.makeText(requireContext(), "Error account not found", Toast.LENGTH_SHORT).show()
            }else{
                showDialogVerifyAccount(email)
            }
        }
        manageaccountsAdapter.onDeleteAccountClick={email->
            if (email.isEmpty()){
                Toast.makeText(requireContext(), "Error account not found", Toast.LENGTH_SHORT).show()
            }else{
                val email = email
                val alertDialog = AlertDialog.Builder(requireContext()).apply {
                    setTitle("Delete account from the app")
                    setMessage("Do you want to delete this account from the app?")
                    setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    setPositiveButton("Yes") { dialog, _ ->
                        viewModel.deleteAccount(email)
                        dialog.dismiss()
                    }
                }
                alertDialog.create()
                alertDialog.show()
            }
        }



    }
    private fun showDialogVerifyAccount(email: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Verifi Account")

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val spinner = Spinner(requireContext()).apply {
            val positions = arrayOf("Unban", "Ban")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, positions)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
        }


        container.addView(spinner)
        builder.setView(container)

        builder.setPositiveButton("Submit") { dialog, which ->
            val selectedStatus = spinner.selectedItem.toString()
            val status = if (selectedStatus == "Unban") "1" else "0"
            viewModel.updateStatus(email, status)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun showCreateAccountDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Create Account")

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.admin_dialog_create_account, null)
        builder.setView(dialogView)

        val edEmail = dialogView.findViewById<EditText>(R.id.edEmail1)
        val edFirstName = dialogView.findViewById<EditText>(R.id.edFirstName1)
        val edLastName = dialogView.findViewById<EditText>(R.id.edLastName1)
        val edPassword = dialogView.findViewById<EditText>(R.id.edPassword1)
        val spposition =dialogView.findViewById<Spinner>(R.id.spinnerPosition)

        val positions = arrayOf("Admin", "User")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, positions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spposition.adapter = adapter

        builder.setPositiveButton("Submit") { dialog, which ->
            val email = edEmail.text.toString().trim()
            val firstName = edFirstName.text.toString().trim()
            val lastName = edLastName.text.toString().trim()
            val password = edPassword.text.toString().trim()
            val imagePath = "https://firebasestorage.googleapis.com/v0/b/secondhandshop-2f45f.appspot.com/o/profileImages%2Flogo-01.png?alt=media&token=3a71279e-eb87-4c52-b1fd-7125311a7611"
            val selectedPosition = spposition.selectedItem.toString().trim()
            val position = if (selectedPosition == "Admin") "1" else "0"

            if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }else {
                val user = User(firstName, lastName, email, imagePath, "1",position )
                viewModel.createAccountWithEmailAndPassword(user,password)
            }

        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }
        builder.show()
    }


}
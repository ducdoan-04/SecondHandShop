package com.example.secondhandshop.fragments.adminCategories

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.secondhandshop.data.CategoryAdmin

import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.viewmodel.admin.CategoryAdminManageProductsViewModel
import com.example.secondhandshop.viewmodel.factory.BaseCategoryProductAdminViewModelFactoryFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class ManageProductsFragment: AdminBaseCategoryProductFragment(){
    @Inject
    lateinit var firestore: FirebaseFirestore

    private val viewModel by viewModels<CategoryAdminManageProductsViewModel>{
        BaseCategoryProductAdminViewModelFactoryFactory(firestore, CategoryAdmin.ManageProducts)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.manageProducts.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showManageLoading()
                    }
                    is Resource.Success -> {
                        manageproductsAdapter.differ.submitList(it.data)
                        hideManageLoading()
                    }
                    is Resource.Error -> {
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                        hideManageLoading()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.manageProductsVerifi.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showManageLoading()
                    }
                    is Resource.Success -> {
                        manageproductsverifiAdapter.differ.submitList(it.data)
                        hideManageLoading()
                    }
                    is Resource.Error -> {
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                        hideManageLoading()
                    }
                    else -> Unit
                }
            }
        }



        manageproductsAdapter.onVerifiClick ={id->
            val id = id
            val alertDialog = AlertDialog.Builder(requireContext()).apply {
                setTitle("Verifi the product")
                setMessage("You want to confirm the shop's products?")
                setNegativeButton("No") { dialog, _ ->
                    viewModel.verifiProduct(id,"0")
                    dialog.dismiss()
                }
                setPositiveButton("Yes") { dialog, _ ->
                    viewModel.verifiProduct(id,"1")
                    dialog.dismiss()
                }
            }
            alertDialog.create()
            alertDialog.show()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.verifyStatusProducts.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        hideLoading()
                        Toast.makeText(requireContext(),"The product has been successfully approved", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }



        manageproductsAdapter.onDeleteClick ={itemToDelete ->
            val IdProduct = itemToDelete
            val alertDialog = AlertDialog.Builder(requireContext()).apply {
                setTitle("Delete item from shop")
                setMessage("Do you want to delete this item from your shop?")
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton("Yes") { dialog, _ ->
                    viewModel.deleteProduct(IdProduct)
                    dialog.dismiss()
                }
            }
            alertDialog.create()
            alertDialog.show()

        }

        lifecycleScope.launchWhenStarted {
            viewModel.deleteProducts.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.offerProductsProgressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.offerProductsProgressBar.visibility = View.INVISIBLE
                        if (it.data!!.isEmpty()){
                            showEmptyProducts()
                            hideOtherViews()
                        }else {
                            hideEmptyProducts()
                            showOtherViews()
                            manageproductsAdapter.differ.submitList(it.data)
                        }
                    }
                    is Resource.Error -> {
                        binding.offerProductsProgressBar.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }


    }

    private fun showEmptyProducts() {
        binding.apply {
//            layout.visibility = View.VISIBLE
        }
    }
    private fun hideEmptyProducts() {
        binding.apply {
//            layout.visibility = View.GONE
        }
    }
    private fun showOtherViews() {
        binding.apply {
            rvListAccounts2.visibility = View.VISIBLE

        }
    }

    private fun hideOtherViews() {
        binding.apply {
            rvListAccounts2.visibility = View.GONE
        }
    }
    private fun hideLoading() {
        binding.apply {
            offerProductsProgressBar.visibility = View.GONE
        }
    }

    private fun showLoading() {
        binding.apply {
            offerProductsProgressBar.visibility = View.VISIBLE
        }
    }

}
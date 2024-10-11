package com.example.secondhandshop.fragments.statusOrder

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.secondhandshop.data.order.CategoryOrderStatus
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.viewmodel.factory.BaseCategoryOrderManagementViewModelFactoryFactory
import com.example.secondhandshop.viewmodel.statusOrder.CategoryOrderManagementViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class DeliveredFragment: BaseCategoryOrderManagementFragment()  {

    @Inject
    lateinit var firestore: FirebaseFirestore


    val viewModel by viewModels<CategoryOrderManagementViewModel>{
        BaseCategoryOrderManagementViewModelFactoryFactory(firestore, CategoryOrderStatus.Delivered)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val idShop = userManager.user?.email ?: ""

        if (idShop.isNotEmpty()){
            viewModel.getListOrderStatus(idShop)
        }else{
            Toast.makeText(requireContext(), "Error: Not found user account", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.listOrder.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        listOrderManagementAdapter.differ.submitList(it.data)
                        hideLoading()
                    }
                    is Resource.Error -> {
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                        hideLoading()
                    }
                    else -> Unit
                }
            }
        }
    }
}
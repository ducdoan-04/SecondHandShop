package com.example.secondhandshop.fragments.adminCategories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.secondhandshop.data.CategoryAdmin
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.viewmodel.admin.CategoryAdminManageReportsViewModel
import com.example.secondhandshop.viewmodel.factory.BaseCategoryReportAdminViewModelFactoryFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class ManageReportsFragment: AdminBaseCategoryReportFragment(){

    @Inject
    lateinit var firestore: FirebaseFirestore

    val viewModel by viewModels<CategoryAdminManageReportsViewModel>{
        BaseCategoryReportAdminViewModelFactoryFactory(firestore, CategoryAdmin.ManageReports)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.manageReport.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showManageLoading()
                    }
                    is Resource.Success -> {
                        managereportsAdapter.differ.submitList(it.data)
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
    }
}
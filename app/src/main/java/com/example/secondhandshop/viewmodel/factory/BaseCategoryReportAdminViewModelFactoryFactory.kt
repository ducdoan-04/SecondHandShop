package com.example.secondhandshop.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.secondhandshop.data.CategoryAdmin
import com.example.secondhandshop.viewmodel.admin.CategoryAdminManageReportsViewModel
import com.google.firebase.firestore.FirebaseFirestore

class BaseCategoryReportAdminViewModelFactoryFactory (
    private val firestore: FirebaseFirestore,
    private val category: CategoryAdmin
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoryAdminManageReportsViewModel(firestore, category) as T
    }
}
package com.example.secondhandshop.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.secondhandshop.data.CategoryAdmin
import com.example.secondhandshop.viewmodel.admin.CategoryAdminManageAccountsViewModel
import com.example.secondhandshop.viewmodel.admin.CategoryAdminManageProductsViewModel
import com.google.firebase.firestore.FirebaseFirestore

class BaseCategoryProductAdminViewModelFactoryFactory (
    private val firestore: FirebaseFirestore,
    private val category: CategoryAdmin
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return CategoryAdminViewModel(firestore, category) as T
        return CategoryAdminManageProductsViewModel(firestore, category) as T
    }
}
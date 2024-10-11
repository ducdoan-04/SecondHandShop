package com.example.secondhandshop.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.secondhandshop.data.CategoryAdmin
import com.example.secondhandshop.viewmodel.admin.CategoryAdminManageAccountsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BaseCategoryAdminViewModelFactoryFactory (
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val category: CategoryAdmin
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return CategoryAdminViewModel(firestore, category) as T
        return CategoryAdminManageAccountsViewModel(firestore,firebaseAuth,category) as T
    }
}
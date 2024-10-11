package com.example.secondhandshop.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.secondhandshop.data.order.CategoryOrderStatus
import com.example.secondhandshop.viewmodel.statusOrder.CategoryOrderManagementViewModel
import com.google.firebase.firestore.FirebaseFirestore

class BaseCategoryOrderManagementViewModelFactoryFactory (
    private val firestore: FirebaseFirestore,
    private val categoryOrder: CategoryOrderStatus
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoryOrderManagementViewModel(firestore, categoryOrder) as T
    }
}
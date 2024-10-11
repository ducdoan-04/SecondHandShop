package com.example.secondhandshop.viewmodel.admin

import androidx.lifecycle.ViewModel
import com.example.secondhandshop.data.Accounts
import com.example.secondhandshop.data.CategoryAdmin
import com.example.secondhandshop.data.Product
import com.example.secondhandshop.data.User
import com.example.secondhandshop.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CategoryAdminViewModel constructor(
    private val firestore: FirebaseFirestore,
    private val category: CategoryAdmin
): ViewModel() {
    private val _manageAccounts = MutableStateFlow<Resource<List<Accounts>>>(Resource.Unspecified())
     val manageAccounts = _manageAccounts.asStateFlow()

    private val _manageProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val manageProducts = _manageProducts.asStateFlow()




    private val  _addAccounts = MutableStateFlow<Resource<Accounts>>(Resource.Unspecified())
    val addAccounts: Flow<Resource<Accounts>> = _addAccounts


    private val _deleteProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val deleteProducts = _deleteProducts.asStateFlow()

    private val _checkProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val checkProducts = _checkProducts.asStateFlow()

    private val _updateProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val updateProducts = _updateProducts.asStateFlow()

    init {
//        getListAccounts()
//        getListProducts()
    }


}
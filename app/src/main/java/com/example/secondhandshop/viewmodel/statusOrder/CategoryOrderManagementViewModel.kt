package com.example.secondhandshop.viewmodel.statusOrder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope


import com.example.secondhandshop.data.order.CategoryOrderStatus
import com.example.secondhandshop.data.order.Order
import com.example.secondhandshop.di.UserManager
import com.example.secondhandshop.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoryOrderManagementViewModel constructor(
    private val firestore: FirebaseFirestore,
    private val categoryOrder: CategoryOrderStatus
):ViewModel() {

    private val _listOrder = MutableStateFlow<Resource<List<Order>>>(Resource.Unspecified())
        val listOrder = _listOrder.asStateFlow()



    fun getListOrderStatus(idShop: String){
        viewModelScope.launch {
            _listOrder.emit(Resource.Unspecified())
        }

        firestore.collection("orders").whereEqualTo("idShop",idShop)
            .whereEqualTo("orderStatus",categoryOrder.categoryOrderStatus).get()
            .addOnSuccessListener {
                val orders = it.toObjects(Order::class.java)
                viewModelScope.launch {
                    _listOrder.emit(Resource.Success(orders))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _listOrder.emit(Resource.Error(it.message.toString()))
                }
            }
    }


   }
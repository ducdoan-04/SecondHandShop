package com.example.secondhandshop.viewmodel.statusOrder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.User
import com.example.secondhandshop.data.order.Order
import com.example.secondhandshop.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManinCategoryOrderManagementViewModel  @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
): ViewModel(){

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user= _user.asStateFlow()

    private val _allOrders = MutableStateFlow<Resource<List<Order>>>(Resource.Unspecified())
    val allOrders = _allOrders.asStateFlow()

    init {
        getUser()
    }
    fun getUser(){
        viewModelScope.launch {
            _user.emit(Resource.Loading())
        }

        firestore.collection("user").document(auth.uid!!).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                user?.let {
                    viewModelScope.launch {
                        _user.emit(Resource.Success(it))
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _user.emit(Resource.Error(it.message.toString()))
                }
            }
    }


    fun getListOrders(idShop: String,statusOrder: String) {
        viewModelScope.launch {
            _allOrders.emit(Resource.Loading())
        }
        firestore.collection("orders").whereEqualTo("idShop", idShop).whereEqualTo("orderStatus", statusOrder ).get()
            .addOnSuccessListener { querySnapshot ->
                val orders = querySnapshot.toObjects(Order::class.java)
                viewModelScope.launch {
                    _allOrders.emit(Resource.Success(orders))
                }
            }.addOnFailureListener { exception ->
                viewModelScope.launch {
                    _allOrders.emit(Resource.Error(exception.message.toString()))
                }
            }
    }

}
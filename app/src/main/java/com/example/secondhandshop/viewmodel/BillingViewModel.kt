package com.example.secondhandshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.Accounts
import com.example.secondhandshop.data.Address
import com.example.secondhandshop.data.CartProduct
import com.example.secondhandshop.data.User
import com.example.secondhandshop.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) :ViewModel(){

    private val _address = MutableStateFlow<Resource<List<Address>>>(Resource.Unspecified())
    val address = _address.asStateFlow()

    private val _idShop = MutableStateFlow<Resource<List<CartProduct>>>(Resource.Unspecified())
    val idShop = _idShop.asStateFlow()

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    init {
        getUserAddresses()
        getIdShop()
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

    fun getUserAddresses(){
        viewModelScope.launch { _address.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("address")
            .addSnapshotListener { value, error ->
                if (error != null){
                    viewModelScope.launch { _address.emit(Resource.Error(error.message.toString())) }
                    return@addSnapshotListener
                }
                val addresses = value?.toObjects(Address::class.java)
                viewModelScope.launch { _address.emit(Resource.Success(addresses!!)) }
            }
    }




    fun getIdShop() {
        viewModelScope.launch {
            _idShop.emit(Resource.Loading())
            try {
                firestore.collection("user").document(auth.uid!!).collection("cart")
                    .limit(1)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            viewModelScope.launch { _idShop.emit(Resource.Error(error.message.toString())) }
                            return@addSnapshotListener
                        }
                        val idshop = value?.toObjects(CartProduct::class.java)
                        viewModelScope.launch { _idShop.emit(Resource.Success(idshop ?: emptyList())) }
                    }
            } catch (e: Exception) {
                _idShop.emit(Resource.Error(e.message.toString()))
            }
        }
    }
}

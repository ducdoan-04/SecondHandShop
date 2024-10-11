package com.example.secondhandshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.Product
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
class UpdateProductViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
):ViewModel(){

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    private val _product = MutableStateFlow<Resource<Product>>(Resource.Unspecified())
    val product = _product.asStateFlow()

    init {
        getUser()
    }

    fun getUser() {
        viewModelScope.launch {
            _user.emit(Resource.Loading())
        }
        firestore.collection("user").document(auth.uid!!)
            .addSnapshotListener{value, error ->
                if (error != null){
                    viewModelScope.launch {
                        _user.emit(Resource.Error(error.message.toString()))
                    }
                }else{
                    val user = value?.toObject(User::class.java)
                    user?.let {
                        viewModelScope.launch {
                            _user.emit(Resource.Success(user))
                        }
                    }
                }

            }
    }

    fun getProductbyId(IdProduct: String){
        viewModelScope.launch {
            _product.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("id", IdProduct)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch {
                        _product.emit(Resource.Error(error.message.toString()))
                    }
                } else if (value != null && !value.isEmpty) {
                    val product = value.documents[0].toObject(Product::class.java)
                    product?.let {
                        viewModelScope.launch {
                            _product.emit(Resource.Success(product))
                        }
                    }
                }
            }
    }



    fun getUser(IdUser: String) {
        viewModelScope.launch {
            _user.emit(Resource.Loading())
        }
        firestore.collection("user").whereEqualTo("email", IdUser)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch {
                        _user.emit(Resource.Error(error.message.toString()))
                    }
                } else if (value != null && !value.isEmpty) {
                    val user = value.documents[0].toObject(User::class.java)
                    user?.let {
                        viewModelScope.launch {
                            _user.emit(Resource.Success(user))
                        }
                    }
                }
            }
    }
}
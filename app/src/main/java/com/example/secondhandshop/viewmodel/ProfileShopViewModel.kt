package com.example.secondhandshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.Product
import com.example.secondhandshop.data.User
import com.example.secondhandshop.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileShopViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
):ViewModel(){

    private val _searchProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val searchProducts = _searchProducts.asStateFlow()

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    private var searchProductDocuments = emptyList<DocumentSnapshot>()


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

    fun getSearchProducts(IdUser: String) {
        viewModelScope.launch { _searchProducts.emit(Resource.Loading()) }

        firestore.collection("Products").whereEqualTo("idUser", IdUser)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch { _searchProducts.emit(Resource.Error(error.message.toString())) }
                } else if (value != null) {
                    searchProductDocuments = value.documents
                    val searchProducts = value.toObjects(Product::class.java)
                    viewModelScope.launch { _searchProducts.emit(Resource.Success(searchProducts)) }
                }
            }
    }




}
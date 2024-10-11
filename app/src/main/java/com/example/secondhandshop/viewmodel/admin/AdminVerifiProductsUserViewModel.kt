package com.example.secondhandshop.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.CartProduct
import com.example.secondhandshop.data.Product
import com.example.secondhandshop.data.User
import com.example.secondhandshop.firebase.FirebaseCommon
import com.example.secondhandshop.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminVerifiProductsUserViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
): ViewModel() {
    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    private var listProductDocuments = emptyList<DocumentSnapshot>()

    private val _verifiProduct = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val verifiProduct = _verifiProduct.asStateFlow()

    private val  _verifyStatusProducts = MutableStateFlow<Resource<Product>>(Resource.Unspecified())
    val verifyStatusProducts: Flow<Resource<Product>> = _verifyStatusProducts
    init {
        getUser()
        getListVerifiProducts("0")
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


    fun getListVerifiProducts(status: String){
        viewModelScope.launch {
            _verifiProduct.emit(Resource.Loading())
        }

        firestore.collection("Products").whereEqualTo("status",status)
            .addSnapshotListener{value, error ->
                if (error != null || value == null){
                    viewModelScope.launch { _verifiProduct.emit(Resource.Error(error?.message.toString())) }
                }else{
                    listProductDocuments = value.documents
                    val verifiProducts = value.toObjects(Product::class.java)
                    viewModelScope.launch {_verifiProduct.emit(Resource.Success(verifiProducts)) }
                }
            }
    }

    private val _verifiDialog = MutableSharedFlow<CartProduct>()
    val verifiDialog = _verifiDialog.asSharedFlow()

    fun verifiProduct1(IdProduct: String, status: String){
        viewModelScope.launch {
            _verifyStatusProducts.emit(Resource.Unspecified())
        }

        val productCollection = firestore.collection("Products")
        productCollection.whereEqualTo("id",IdProduct)
            .get()
            .addOnSuccessListener {querySnapshot ->
                for (document in querySnapshot.documents) {
                    val IdProduct = document.id
                    productCollection.document(IdProduct)
                        .update("status",status)
                        .addOnSuccessListener {
                            _verifyStatusProducts.value = Resource.Success(Product())
                        }
                        .addOnFailureListener {e->
                            _verifyStatusProducts.value = Resource.Error(e.message.toString())
                        }
                }
            }
            .addOnFailureListener { e->
                _verifyStatusProducts.value = Resource.Error(e.message.toString())
            }
    }

    fun verifiProduct(IdProduct: String, status: String) {
        viewModelScope.launch {
            _verifyStatusProducts.emit(Resource.Unspecified())
        }

        val productCollection = firestore.collection("Products")
        productCollection.whereEqualTo("id", IdProduct)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val productId = document.id
                        productCollection.document(productId)
                            .update("status", status)
                            .addOnSuccessListener {
                                viewModelScope.launch {
                                    _verifyStatusProducts.emit(Resource.Success(Product()))
//                                    _verifyStatusProducts.value = Resource.Success(Product())
                                }
                            }
                            .addOnFailureListener { e ->
                                viewModelScope.launch {
                                    _verifyStatusProducts.emit(Resource.Error(e.message.toString()))
                                }
                            }
                    }
                } else {
                    viewModelScope.launch {
                        _verifyStatusProducts.emit(Resource.Error("Not found product with this id"))
                    }
                }
            }
            .addOnFailureListener { e ->
                viewModelScope.launch {
                    _verifyStatusProducts.emit(Resource.Error(e.message.toString()))
                }
            }
    }

}
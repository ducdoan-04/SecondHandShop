package com.example.secondhandshop.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.CartProduct
import com.example.secondhandshop.data.CategoryAdmin
import com.example.secondhandshop.data.Product
import com.example.secondhandshop.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class CategoryAdminManageProductsViewModel constructor(
    private val firestore: FirebaseFirestore,
    private val category: CategoryAdmin
): ViewModel(){
    private val _manageProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val manageProducts = _manageProducts.asStateFlow()

    private val _manageProductsVerifi = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val manageProductsVerifi = _manageProductsVerifi.asStateFlow()

    init {
        getListProducts()
        getProductsVerifi()
    }
    fun getListProducts(){
        viewModelScope.launch {
            _manageProducts.emit(Resource.Unspecified())
        }
        firestore.collection("Products").get()
            .addOnSuccessListener {
                val product = it.toObjects(Product::class.java)
                viewModelScope.launch {
                    _manageProducts.emit(Resource.Success(product))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _manageProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    fun getProductsVerifi(){
        viewModelScope.launch {
            _manageProductsVerifi.emit(Resource.Unspecified())
        }
        firestore.collection("Products").whereEqualTo("status", "0").get()
            .addOnSuccessListener {
                val productverifi = it.toObjects(Product::class.java)
                viewModelScope.launch {
                    _manageProductsVerifi.emit(Resource.Success(productverifi))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _manageProductsVerifi.emit(Resource.Error(it.message.toString()))
                }
            }
    }


    private val _deleteProduct = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val deleteProducts = _deleteProduct.asStateFlow()

    fun deleteProduct(idProduct: String) {
        if (idProduct != null){
            firestore.collection("Products").whereEqualTo("id", idProduct).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        for (document in querySnapshot.documents) {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    viewModelScope.launch {
                                        _deleteProduct.emit(Resource.Error("Product is deleted success"))
                                    }
                                }
                                .addOnFailureListener { e ->
                                    viewModelScope.launch {
                                        _deleteProduct.emit(Resource.Error(e.message.toString()))
                                    }
                                }
                        }
                    } else {
                        viewModelScope.launch {
                            _deleteProduct.emit(Resource.Error("Product not found"))
                        }
                    }
                }
                .addOnFailureListener { e ->
                    viewModelScope.launch {
                        _deleteProduct.emit(Resource.Error(e.message.toString()))
                    }
                }
        } else {
            viewModelScope.launch {
                _deleteProduct.emit(Resource.Error("Product not found"))
            }
        }
    }

    private val  _verifyStatusProducts = MutableStateFlow<Resource<Product>>(Resource.Unspecified())
    val verifyStatusProducts: Flow<Resource<Product>> = _verifyStatusProducts

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
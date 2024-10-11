package com.example.secondhandshop.viewmodel

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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteProductViewModel  @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
): ViewModel(){
    private val _deleteProduct = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val deleteProducts = _deleteProduct.asStateFlow()

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    private var searchProductDocuments = emptyList<DocumentSnapshot>()

    init {
        getUser()
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


    fun getSearchProducts(IdUser: String) {
        viewModelScope.launch { _deleteProduct.emit(Resource.Loading()) }

        firestore.collection("Products").whereEqualTo("idUser", IdUser)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch { _deleteProduct.emit(Resource.Error(error.message.toString())) }
                } else if (value != null) {
                    searchProductDocuments = value.documents
                    val searchProducts = value.toObjects(Product::class.java)
                    viewModelScope.launch { _deleteProduct.emit(Resource.Success(searchProducts)) }
                }
            }
    }

     fun getListDeleteProducts(IdUser: String){
        viewModelScope.launch { _deleteProduct.emit(Resource.Loading()) }
        firestore.collection("Products").whereEqualTo("idUser", IdUser)
            .addSnapshotListener{value, error ->
                if (error != null || value == null){
                    viewModelScope.launch { _deleteProduct.emit(Resource.Error(error?.message.toString())) }
                }else{
                    searchProductDocuments = value.documents
                    val deleteProducts = value.toObjects(Product::class.java)
                    viewModelScope.launch { _deleteProduct.emit(Resource.Success(deleteProducts))}
                }

            }

    }
    private val _deleteDialog = MutableSharedFlow<CartProduct>()
    val deleteDialog = _deleteDialog.asSharedFlow()
    private var deleteProductDocuments = emptyList<DocumentSnapshot>()

//    fun deleteProduct(deleteProduct: Product){
//        val index = deleteProducts.value.data?.indexOf(deleteProduct)
//
//        if (index != null && index != -1) {
//            val documentId = deleteProductDocuments[index].id
//            firestore.collection("Product").document(documentId).delete()
//        }
//    }
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


}
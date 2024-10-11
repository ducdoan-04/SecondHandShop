package com.example.secondhandshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.Product
import com.example.secondhandshop.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchProductsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _searchProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val searchProducts = _searchProducts.asStateFlow()

    private var searchProductDocuments = emptyList<DocumentSnapshot>()

    init {
//        getSearchProducts()
    }

    fun getSearchProducts() {
        viewModelScope.launch { _searchProducts.emit(Resource.Loading()) }

        firestore.collection("Products")
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

    fun searchProductsByName(query: String) {
        viewModelScope.launch { _searchProducts.emit(Resource.Loading()) }

        firestore.collection("Products")
            .orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
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

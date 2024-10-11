package com.example.secondhandshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.Favourite
import com.example.secondhandshop.data.User
import com.example.secondhandshop.firebase.FirebaseCommon
import com.example.secondhandshop.helper.getProductPrice
import com.example.secondhandshop.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductFavouriteViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
): ViewModel() {

    private val _productFavourites = MutableStateFlow<Resource<List<Favourite>>>(Resource.Unspecified())
    val productFavourites = _productFavourites.asStateFlow()

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    val productsPrice = productFavourites.map {
        when(it){
            is Resource.Success -> {
                calculatePrice(it.data!!)
            }
            else -> null
        }
    }

    private val _deleteDialog = MutableSharedFlow<Favourite>()
    val deleteDialog = _deleteDialog.asSharedFlow()

    private var productFavouritDocuments = emptyList<DocumentSnapshot>()

    private fun calculatePrice(data: List<Favourite>): Float {
        return data.sumByDouble { productFavourite ->
            (productFavourite.product.offerPercentage.getProductPrice(productFavourite.product.price) * 1).toDouble()
        }.toFloat()
    }

    fun deleteFavouriteProduct(productFavourite: Favourite){
        val index = productFavourites.value.data?.indexOf(productFavourite)
        if (index != null && index != -1) {
            val documentId = productFavouritDocuments[index].id
            firestore.collection("user").document(auth.uid!!).collection("favourite")
                .document(documentId).delete()
        }
    }

    init {
        getFavouriteProducts()
        getUser()
    }

    private fun getFavouriteProducts(){
        viewModelScope.launch { _productFavourites.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("favourite")
            .addSnapshotListener{value, error ->
                if (error != null || value == null){
                    viewModelScope.launch { _productFavourites.emit(Resource.Error(error?.message.toString())) }
                }else{
                    productFavouritDocuments = value.documents
                    val productFavourite =value.toObjects(Favourite::class.java)
                    viewModelScope.launch { _productFavourites.emit(Resource.Success(productFavourite)) }
                }

            }

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


}
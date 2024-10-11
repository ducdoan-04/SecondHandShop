package com.example.secondhandshop.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.Address
import com.example.secondhandshop.data.CartProduct
import com.example.secondhandshop.data.Comment
import com.example.secondhandshop.data.CommentDTO
import com.example.secondhandshop.data.Favourite
import com.example.secondhandshop.data.Report
import com.example.secondhandshop.data.User
import com.example.secondhandshop.firebase.FirebaseCommon
import com.example.secondhandshop.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon,
    private val storage: StorageReference,
    app: Application
): ViewModel() {

    private val _addToCart = MutableStateFlow<Resource<CartProduct>>(Resource.Unspecified())
        val addToCart = _addToCart.asStateFlow()

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    private val _userclient = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val userclient = _userclient.asStateFlow()

    private val _addToReport = MutableStateFlow<Resource<Report>>(Resource.Unspecified())
        val addToReport = _addToReport.asStateFlow()

    private val _addToFavourite = MutableStateFlow<Resource<Favourite>>(Resource.Unspecified())
        val addToFavourite = _addToFavourite.asStateFlow()

    private val _addComment = MutableStateFlow<Resource<Comment>>(Resource.Unspecified())
    val addComment = _addComment.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val _listComment = MutableStateFlow<Resource<List<Comment>>>(Resource.Unspecified())
    val listComment = _listComment.asStateFlow()

    fun addUpdateProductInCart(cartProduct: CartProduct){
        viewModelScope.launch { _addToCart.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("cart")
            .whereEqualTo("product.id", cartProduct.product.id).get()
            .addOnSuccessListener {
                it.documents.let {
                    if (it.isEmpty()){//Add new product
                        addNewProduct(cartProduct)
                    }else{
                        val product = it.first().toObject(CartProduct::class.java)
                        if (product == cartProduct){ // Increase the quantity
                            val documentId = it.first().id
                            increaseQuantity(documentId, cartProduct)
                        }else{//Add new product
                            addNewProduct(cartProduct)
                        }
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch { _addToCart.emit(Resource.Error(it.message.toString())) }
            }
    }

    private fun addNewProduct(cartProduct: CartProduct){
        firebaseCommon.addProductToCart(cartProduct){addedProduct, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToCart.emit(Resource.Success(addedProduct!!))
                else
                    _addToCart.emit(Resource.Error(e.message.toString()))
            }

        }
    }

    private fun increaseQuantity(documentId: String, cartProduct: CartProduct){
        firebaseCommon.increaseQuantity(documentId){ _, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToCart.emit(Resource.Success(cartProduct!!))
                else
                    _addToCart.emit(Resource.Error(e.message.toString()))
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

    fun addFavourite1 (favorite: Favourite){
        val validateInputs = validateInput(favorite)

        if (validateInput(favorite)) {
            viewModelScope.launch { _addToFavourite.emit(Resource.Loading()) }

            firestore.collection("user").document(auth.uid!!).collection("favourite").document()
                .set(favorite)
                .addOnSuccessListener {
                    viewModelScope.launch { _addToFavourite.emit(Resource.Success(favorite)) }
                }.addOnFailureListener {
                    viewModelScope.launch { _addToFavourite.emit(Resource.Error(it.message.toString())) }
                }
        }else{
            viewModelScope.launch {
                _error.emit("All fields are required")
            }
        }
    }


    fun addFavourite(favorite: Favourite) {
        if (validateInput(favorite)) {
            viewModelScope.launch { _addToFavourite.emit(Resource.Loading()) }

            firestore.collection("user").document(auth.uid!!).collection("favourite")
                .whereEqualTo("idProduct", favorite.idProduct).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        addNewFavourite(favorite)
                    } else {
                        viewModelScope.launch {
                            _addToFavourite.emit(Resource.Error("Product already in favourites"))
                        }
                    }
                }.addOnFailureListener {
                    viewModelScope.launch {
                        _addToFavourite.emit(Resource.Error(it.message.toString()))
                    }
                }
        } else {
            viewModelScope.launch {
                _error.emit("All fields are required")
            }
        }
    }

    private fun addNewFavourite(favorite: Favourite) {
        firestore.collection("user").document(auth.uid!!).collection("favourite").document()
            .set(favorite)
            .addOnSuccessListener {
                viewModelScope.launch { _addToFavourite.emit(Resource.Success(favorite)) }
            }.addOnFailureListener {
                viewModelScope.launch { _addToFavourite.emit(Resource.Error(it.message.toString())) }
            }
    }


    fun addReport(report: Report) {
        if (validateInput2(report)) {
            viewModelScope.launch { _addToReport.emit(Resource.Loading()) }

            firestore.collection("user").document(auth.uid!!).collection("report")
                .whereEqualTo("idProduct", report.idProduct).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        addNewReport(report)
                    } else {
                        viewModelScope.launch {
                            _addToReport.emit(Resource.Error("Product already in report"))
                        }
                    }
                }.addOnFailureListener {
                    viewModelScope.launch {
                        _addToReport.emit(Resource.Error(it.message.toString()))
                    }
                }
        } else {
            viewModelScope.launch {
                _error.emit("All fields are required")
            }
        }
    }

    private fun addNewReport(report: Report) {
        firestore.runBatch { batch ->
            firestore.collection("user").document(auth.uid!!).collection("report").document().set(report)
            firestore.collection("report").document().set(report)
        }.addOnSuccessListener {
                viewModelScope.launch { _addToReport.emit(Resource.Success(report)) }
            }.addOnFailureListener {
                viewModelScope.launch { _addToReport.emit(Resource.Error(it.message.toString())) }
            }
    }


    fun addComment(comment: Comment) {
        if (validateInput3(comment)) {
            viewModelScope.launch { _addComment.emit(Resource.Loading()) }

            addNewComment(comment)
        } else {
            viewModelScope.launch {
                _error.emit("All fields are required")
            }
        }
    }

    private fun addNewComment(comment: Comment) {
        firestore.collection("comment").document().set(comment)
            .addOnSuccessListener {
                viewModelScope.launch { _addComment.emit(Resource.Success(comment)) }
            }
            .addOnFailureListener {
                viewModelScope.launch { _addComment.emit(Resource.Error(it.message.toString())) }
            }
    }




    private fun validateInput(favorite: Favourite): Boolean {
        return favorite.idUser.trim().isNotEmpty() &&
                favorite.idProduct.trim().isNotEmpty()
    }

    private fun validateInput2(report: Report): Boolean {
        return report.idUser.trim().isNotEmpty() &&
                report.idProduct.trim().isNotEmpty()
    }

    private fun validateInput3(comment: Comment): Boolean {
        return comment.idUser.trim().isNotEmpty() &&
                comment.idProduct.trim().isNotEmpty()&&
                (comment.content?.trim()?.isNotEmpty() ?: "") as Boolean
    }

    fun getCommentByIdProduct(idProduct: String) {
        viewModelScope.launch {
            _listComment.emit(Resource.Loading())
            try {
                val snapshot = firestore.collection("comment").whereEqualTo("idProduct", idProduct).get().await()
                val commentDTOs = snapshot.toObjects(CommentDTO::class.java)
                val comments = commentDTOs.map { it.toComment() }
                _listComment.emit(Resource.Success(comments))
            } catch (e: Exception) {
                _listComment.emit(Resource.Error(e.message.toString()))
            }
        }
    }




    fun getUserClient(): LiveData<Resource<User>> {
        val userLiveData = MutableLiveData<Resource<User>>()
        viewModelScope.launch {
            userLiveData.value = Resource.Loading()

            firestore.collection("user").document(auth.uid!!).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        userLiveData.value = Resource.Success(user)
                    } else {
                        userLiveData.value = Resource.Error("User not found")
                    }
                }.addOnFailureListener {
                    userLiveData.value = Resource.Error(it.message.toString())
                }
        }
        return userLiveData
    }


}
package com.example.secondhandshop.viewmodel.admin

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.SecondHandShopApplication
import com.example.secondhandshop.data.Accounts


import com.example.secondhandshop.util.RegisterValidation
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.util.validateEmail
import com.google.android.gms.tasks.Task

import com.google.firebase.auth.FirebaseAuth


import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AdminUpdateUserAccountViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: StorageReference,
    app: Application
): AndroidViewModel(app) {
    private val _userAccount = MutableStateFlow<Resource<Accounts>>(Resource.Unspecified())
    val userAccount = _userAccount.asStateFlow()

    private val _updateUserAccount = MutableStateFlow<Resource<Accounts>>(Resource.Unspecified())
    val updateUserAccount = _updateUserAccount.asStateFlow()

    private val functions = FirebaseFunctions.getInstance()

    fun getUserAccount(email: String) {
        viewModelScope.launch {
            _userAccount.emit(Resource.Loading())
        }
        firestore.collection("user").whereEqualTo("email", email)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch {
                        _userAccount.emit(Resource.Error(error.message.toString()))
                    }
                } else if (value != null && !value.isEmpty) {
                    val userAccount = value.documents[0].toObject(Accounts::class.java)
                    userAccount?.let {
                        viewModelScope.launch {
                            _userAccount.emit(Resource.Success(userAccount))
                        }
                    }
                }
            }
    }


    fun updateUser(userAccount: Accounts,imageUri: Uri?, email: String){
        val areInputsValid = validateEmail(userAccount.email) is RegisterValidation.Success
                && userAccount.firstName.trim().isNotEmpty()
                &&userAccount.lastName.trim().isNotEmpty()

        if (!areInputsValid){
            viewModelScope.launch {
                _userAccount.emit(Resource.Error("Check your inputs"))
            }
            return
        }

        viewModelScope.launch {
            _updateUserAccount.emit(Resource.Loading())
        }

        if (imageUri == null){
            saveUserInformation(userAccount, true,email)
        }else{
            saveUserInformationWithNewImage(userAccount, imageUri,email)
        }
    }

    fun getUserUID(email: String): Task<String> {
        val data = hashMapOf(
            "email" to email
        )

        return functions
            .getHttpsCallable("getUserUID")
            .call(data)
            .continueWith { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Unknown error")
                }
                val result = task.result?.data as? Map<*, *> ?: throw Exception("Invalid response")
                result["uid"] as? String ?: throw Exception("UID not found")
            }
    }


    private fun saveUserInformation(userAccount: Accounts, shouldRetrievedOldImage: Boolean, email: String) {
        val documentRef = firestore.collection("user").whereEqualTo("email", email).limit(1)

        documentRef.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val documentSnapshot = querySnapshot.documents[0]
                val documentId = documentSnapshot.id

                firestore.runTransaction { transaction ->
                    if (shouldRetrievedOldImage) {
                        val currentUser = transaction.get(firestore.collection("user").document(documentId)).toObject(Accounts::class.java)
                        val newUser = userAccount.copy(imagePath = currentUser?.imagePath ?: "")
                        transaction.set(firestore.collection("user").document(documentId), newUser)
                    } else {
                        transaction.set(firestore.collection("user").document(documentId), userAccount)
                    }
                }.addOnSuccessListener {
                    viewModelScope.launch {
                        _updateUserAccount.emit(Resource.Success(userAccount))
                    }
                }.addOnFailureListener { e ->
                    viewModelScope.launch {
                        _updateUserAccount.emit(Resource.Error(e.message.toString()))
                    }
                }
            } else {
                viewModelScope.launch {
                    _userAccount.emit(Resource.Error("Not found user accounts"))
                }
            }
        }.addOnFailureListener { e ->
            viewModelScope.launch {
                _userAccount.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    private fun saveUserInformationWithNewImage(userAccount: Accounts, imageUri: Uri,email: String) {
        viewModelScope.launch {
            try {
                val imageBitmap = MediaStore.Images.Media.getBitmap(getApplication<SecondHandShopApplication>().contentResolver, imageUri)
                val byteArrayOutputStream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG,96, byteArrayOutputStream)
                val imageByteArray = byteArrayOutputStream.toByteArray()
                val imageDirectory = storage.child("profileImages/${auth.uid}/${UUID.randomUUID()}")
                val result = imageDirectory.putBytes(imageByteArray).await()
                val imageUrl = result.storage.downloadUrl.await().toString()
                saveUserInformation(userAccount.copy(imagePath = imageUrl),false,email)
            }catch (e: Exception){
                viewModelScope.launch {
                    _userAccount.emit(Resource.Error(e.message.toString()))
                }
            }
        }
    }
//Task is not yet complete

}
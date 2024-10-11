package com.example.secondhandshop.viewmodel.admin

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.Accounts
import com.example.secondhandshop.data.CategoryAdmin
import com.example.secondhandshop.data.User
import com.example.secondhandshop.fragments.shopping.AdminHomeFragmentDirections
import com.example.secondhandshop.util.Constants
import com.example.secondhandshop.util.RegisterFailedsState
import com.example.secondhandshop.util.RegisterValidation
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.util.validateEmail
import com.example.secondhandshop.util.validatePassword
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


class CategoryAdminManageAccountsViewModel constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val category: CategoryAdmin,
): ViewModel(){
    private val _manageAccounts = MutableStateFlow<Resource<List<Accounts>>>(Resource.Unspecified())
    val manageAccounts = _manageAccounts.asStateFlow()
//    val manageAccounts : StateFlow<Resource<List<Accounts>>> = _manageAccounts.asStateFlow()

    private val  _createAccount = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val createAccount: Flow<Resource<User>> = _createAccount

    private val _validation = Channel<RegisterFailedsState>()
    val validation = _validation.receiveAsFlow()

    private val  _verifyacccount = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val verifyacccount: Flow<Resource<User>> = _verifyacccount

    private val  _deleteAccount = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val deleteAccount: Flow<Resource<User>> = _deleteAccount

    init {
        getListAccount()
    }

    fun getListAccount() {
        viewModelScope.launch {
            _manageAccounts.emit(Resource.Unspecified())
        }

        firestore.collection("user").get()
            .addOnSuccessListener {
                val accounts = it.toObjects(Accounts::class.java)
                viewModelScope.launch {
                    _manageAccounts.emit(Resource.Success(accounts))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _manageAccounts.emit(Resource.Error(it.message.toString()))
                }
            }
    }


    fun createAccountWithEmailAndPassword(user: User, password: String){
        if (checkValidation(user, password)) {

            runBlocking {
                _createAccount.emit(Resource.Loading())
            }
            firebaseAuth.createUserWithEmailAndPassword(user.email, password)
                .addOnSuccessListener {
                    it.user?.let {
                        saveUserInfo(it.uid, user)
                    }
                }.addOnFailureListener {
                    _createAccount.value = Resource.Error(it.message.toString())
                }
        } else {
            val registerFieldsState = RegisterFailedsState(
                validateEmail(user.email), validatePassword(password)
            )
            runBlocking {
                _validation.send(registerFieldsState)
            }
        }
    }

    private fun saveUserInfo(userUid: String, user: User) {
        firestore.collection(Constants.USER_COLLECTION)
            .document(userUid)
            .set(user)
            .addOnSuccessListener {
                _createAccount.value = Resource.Success(user)

            }.addOnFailureListener {
                _createAccount.value = Resource.Error(it.message.toString())
            }
    }

    private fun checkValidation(user: User, password: String): Boolean {
        val emailValidation = validateEmail(user.email)
        val passwordValidation = validatePassword(password)
        val shouldRegister = emailValidation is RegisterValidation.Success &&
                passwordValidation is RegisterValidation.Success

        return shouldRegister
    }

    fun updateStatus(email: String, status: String){
        viewModelScope.launch {
            _verifyacccount.emit(Resource.Unspecified())
        }
        val userCollection = firestore.collection("user")
        userCollection.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val userId = document.id
                    userCollection.document(userId)
                        .update("status", status)
                        .addOnSuccessListener {
                            _verifyacccount.value = Resource.Success(User())

                        }
                        .addOnFailureListener { e ->
                            _verifyacccount.value = Resource.Error(e.message.toString())
                        }
                }
            }
            .addOnFailureListener { e ->
                _verifyacccount.value = Resource.Error(e.message.toString())
            }
    }

    fun deleteAccount(email: String) {
        if (email != null){
            firestore.collection("user").whereEqualTo("email", email).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        for (document in querySnapshot.documents) {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    viewModelScope.launch {
                                        _deleteAccount.emit(Resource.Error("Account is deleted success"))
                                    }
                                }
                                .addOnFailureListener { e ->
                                    viewModelScope.launch {
                                        _deleteAccount.emit(Resource.Error(e.message.toString()))
                                    }
                                }
                        }
                    } else {
                        viewModelScope.launch {
                            _deleteAccount.emit(Resource.Error("Account not found"))
                        }
                    }
                }
                .addOnFailureListener { e ->
                    viewModelScope.launch {
                        _deleteAccount.emit(Resource.Error(e.message.toString()))
                    }
                }
        } else {
            viewModelScope.launch {
                _deleteAccount.emit(Resource.Error("Account not found"))
            }
        }
    }


}
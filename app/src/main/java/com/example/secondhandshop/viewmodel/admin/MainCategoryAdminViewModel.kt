package com.example.secondhandshop.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.Accounts
import com.example.secondhandshop.data.Product
import com.example.secondhandshop.data.User
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.viewmodel.PagingInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainCategoryAdminViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
):ViewModel(){

    private val _homeAccounts = MutableStateFlow<Resource<List<Accounts>>>(Resource.Unspecified())
    val homeAccounts: StateFlow<Resource<List<Accounts>>> = _homeAccounts

    private val _homeProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val homeProducts: StateFlow<Resource<List<Product>>> = _homeProducts

    private val _totalAccountCount = MutableStateFlow<Resource<Int>>(Resource.Unspecified())
    val totalAccountCount: StateFlow<Resource<Int>> = _totalAccountCount

    private val _totalProductCount = MutableStateFlow<Resource<Int>>(Resource.Unspecified())
    val totalProductCount: StateFlow<Resource<Int>> = _totalProductCount

    private val _totalReportCount = MutableStateFlow<Resource<Int>>(Resource.Unspecified())
    val totalReportCount: StateFlow<Resource<Int>> = _totalReportCount

    private val _totalRevenue = MutableStateFlow<Resource<Double>>(Resource.Unspecified())
    val totalRevenue: StateFlow<Resource<Double>> = _totalRevenue


    private val _userAdmin = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val userAdmin = _userAdmin.asStateFlow()
//    revenue

    private val pagingInfo = PagingInfo()

    init {
        getUserAdmin()
        fetchHomeAccounts()
        fecthHomeProducts()
        getCountTotalAccount()
        getCountTotalProduct()
        getCountTotalReport()
        getTotalTotalRevenue()
    }

    private fun fecthHomeProducts() {
        viewModelScope.launch {
            _homeProducts.emit(Resource.Loading())
        }
        firestore.collection("Products").get()
            .addOnSuccessListener { result ->
                val homeProductsList = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _homeProducts.emit(Resource.Success(homeProductsList))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _homeProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    private fun fetchHomeAccounts() {
        viewModelScope.launch {
            _homeAccounts.emit(Resource.Loading())
        }
        firestore.collection("user").get()
            .addOnSuccessListener { result->
                val homeAccountsList = result.toObjects(Accounts::class.java)
                viewModelScope.launch {
                    _homeAccounts.emit(Resource.Success(homeAccountsList))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _homeAccounts.emit(Resource.Error(it.message.toString()))
                }
            }

    }

    fun getCountTotalAccount() {
        viewModelScope.launch {
            _totalAccountCount.emit(Resource.Loading())
        }
        firestore.collection("user").get()
            .addOnSuccessListener { result ->
                val count = result.size()
                viewModelScope.launch {
                    _totalAccountCount.emit(Resource.Success(count))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _totalAccountCount.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    private fun getCountTotalReport() {
        viewModelScope.launch {
            _totalReportCount.emit(Resource.Loading())
        }
        firestore.collection("report").get()
            .addOnSuccessListener { result ->
                val count = result.size()
                viewModelScope.launch {
                    _totalReportCount.emit(Resource.Success(count))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _totalReportCount.emit(Resource.Error(it.message.toString()))
                }
            }

    }

    private fun getCountTotalProduct() {
        viewModelScope.launch {
            _totalProductCount.emit(Resource.Loading())
        }
        firestore.collection("Products").get()
            .addOnSuccessListener { result ->
                val count = result.size()
                viewModelScope.launch {
                    _totalProductCount.emit(Resource.Success(count))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _totalProductCount.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    private fun getTotalTotalRevenue() {
        viewModelScope.launch {
            _totalRevenue.emit(Resource.Loading())
        }
        firestore.collection("orders").get()
            .addOnSuccessListener { result ->
                var totalRevenue = 0.0
                for (document in result) {
                    val totalPrice = document.getDouble("totalPrice") ?: 0.0
                    totalRevenue += totalPrice
                }
                viewModelScope.launch {
                    _totalRevenue.emit(Resource.Success(totalRevenue))
                }
            }.addOnFailureListener { exception ->
                viewModelScope.launch {
                    _totalRevenue.emit(Resource.Error(exception.message.toString()))
                }
            }
    }

    fun getUserAdmin(){
        viewModelScope.launch {
            _userAdmin.emit(Resource.Loading())
        }

        firestore.collection("user").document(auth.uid!!).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                user?.let {
                    viewModelScope.launch {
                        _userAdmin.emit(Resource.Success(it))
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _userAdmin.emit(Resource.Error(it.message.toString()))
                }
            }
    }

}
package com.example.secondhandshop.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.CategoryAdmin
import com.example.secondhandshop.data.Report
import com.example.secondhandshop.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryAdminManageReportsViewModel constructor(
    private val firestore: FirebaseFirestore,
    private val category: CategoryAdmin
): ViewModel(){
    private val _manageReports = MutableStateFlow<Resource<List<Report>>>(Resource.Unspecified())
    val manageReport = _manageReports.asStateFlow()

    init {
        getListReports()
    }

    private fun getListReports() {
        viewModelScope.launch {
            _manageReports.emit(Resource.Unspecified())
        }
        firestore.collection("report").get()
            .addOnSuccessListener {
                val report = it.toObjects(Report::class.java)
                viewModelScope.launch {
                    _manageReports.emit(Resource.Success(report))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _manageReports.emit(Resource.Error(it.message.toString()))
                }
            }
    }
}
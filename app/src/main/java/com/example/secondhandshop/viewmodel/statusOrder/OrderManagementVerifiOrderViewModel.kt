package com.example.secondhandshop.viewmodel.statusOrder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondhandshop.data.order.Order
import com.example.secondhandshop.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OrderManagementVerifiOrderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
): ViewModel() {
    private val  _verifyStatusOrder = MutableStateFlow<Resource<Order>>(Resource.Unspecified())
    val verifyStatusOrder: Flow<Resource<Order>> = _verifyStatusOrder


    fun verifiOrder(idOrder: Long, uidClient: String, status: String) {
        viewModelScope.launch {
            _verifyStatusOrder.emit(Resource.Loading())
        }

        // Tìm tài liệu order với điều kiện orderId = idOrder
        firestore.collection("orders").whereEqualTo("orderId", idOrder).get()
            .addOnSuccessListener { documents ->
                val batch = firestore.batch()
                for (document in documents) {
                    // Điều chỉnh trạng thái đơn hàng
                    batch.update(document.reference, "orderStatus", status)
                }

                // Sau khi hoàn thành, cập nhật tài liệu trong collection user/orders
                firestore.collection("user").document(uidClient).collection("orders").get()
                    .addOnSuccessListener { userOrders ->
                        for (userOrder in userOrders) {
                            if (userOrder.getLong("orderId") == idOrder) {
                                batch.update(userOrder.reference, "orderStatus", status)
                            }
                        }

                        // Commit batch để áp dụng các thay đổi
                        batch.commit()
                            .addOnSuccessListener {
                                viewModelScope.launch {
                                    _verifyStatusOrder.emit(Resource.Success(Order()))
                                }
                            }
                            .addOnFailureListener { e ->
                                viewModelScope.launch {
                                    _verifyStatusOrder.emit(Resource.Error(e.message ?: "Error updating status"))
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch {
                            _verifyStatusOrder.emit(Resource.Error(e.message ?: "Error fetching user orders"))
                        }
                    }
            }
            .addOnFailureListener { e ->
                viewModelScope.launch {
                    _verifyStatusOrder.emit(Resource.Error(e.message ?: "Error fetching orders"))
                }
            }
    }

}
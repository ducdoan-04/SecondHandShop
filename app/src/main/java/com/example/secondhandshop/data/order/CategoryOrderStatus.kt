package com.example.secondhandshop.data.order

sealed class CategoryOrderStatus(val categoryOrderStatus: String) {
    object Ordered: CategoryOrderStatus("Ordered")		//Đã đặt hàng
    object Confirmed: CategoryOrderStatus("Confirmed")		//Đã xác nhận
    object Shipped: CategoryOrderStatus("Shipped")		//Đã vận chuyển
    object Delivered: CategoryOrderStatus("Delivered")		//Đã giao hàng
    object Returned: CategoryOrderStatus("Returned")		//Trả lại
    object Canceled: CategoryOrderStatus("Canceled") 		//Đã hủy

}
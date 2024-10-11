package com.example.secondhandshop.data.order

import android.os.Parcelable
import com.example.secondhandshop.data.Address
import com.example.secondhandshop.data.CartProduct
import java.text.SimpleDateFormat
import java.util.Date
import java.util.*
import kotlin.random.Random.Default.nextLong
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    val orderStatus: String = "",
    val totalPrice: Float = 0f,
    val products: List<CartProduct> =  emptyList(),
    val address: Address = Address(),
    val idShop: String="",
    val idClient: String="",
    val uidClient: String="",
    val typePayment:String="",
    val date: String =  SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()),
    val orderId: Long = nextLong(0, 100_000_000_000) + totalPrice.toLong(),
): Parcelable
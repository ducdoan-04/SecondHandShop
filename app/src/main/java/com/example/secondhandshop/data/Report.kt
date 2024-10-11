package com.example.secondhandshop.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Report(
    val product: Product,
    val id: String,
    val idUser: String,
    val idProduct: String,
    val content: String? = null,
    val status: String? = null,
): Parcelable {
    constructor(): this(Product(),"0","","","","")
}

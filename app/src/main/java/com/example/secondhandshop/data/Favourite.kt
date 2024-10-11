package com.example.secondhandshop.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Favourite(
    val product: Product,
    val id: String,
    val idUser: String,
    val idProduct: String,
): Parcelable {
    constructor(): this(Product(),"0","","")
}

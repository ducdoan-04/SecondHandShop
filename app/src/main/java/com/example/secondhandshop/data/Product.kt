package com.example.secondhandshop.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Float,
    val status: String ="0",
    val idUser: String,
    val quantityProducts: Int,
    val offerPercentage: Float? = null,
    val description: String? = null,
    val colors: List<Int>? = null,
    val sizes: List<String>? = null,
    val images: List<String>,

    ): Parcelable{
    constructor(): this("0","","",0f,"0","",0, images = emptyList())
}
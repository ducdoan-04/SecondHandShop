package com.example.secondhandshop.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
data class Comment(
    var id: String,
    val idUser: String,
    val idProduct: String,
    val content: String? = null,
    val user: User? = null,
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
) : Parcelable

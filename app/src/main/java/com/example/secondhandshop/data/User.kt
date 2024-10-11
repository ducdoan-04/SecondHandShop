package com.example.secondhandshop.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val firstName : String,
    val lastName: String,
    val email: String,
    var imagePath: String="",
    var status: String ="",
    var position: String = ""
): Parcelable {
    constructor():this("","","","","","")
}

package com.example.secondhandshop.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CommentDTO(
    var id: String = "",
    val idUser: String = "",
    val idProduct: String = "",
    val content: String? = null,
    val user: User? = null,
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
) {
    fun toComment(): Comment {
        return Comment(
            id = id,
            idUser = idUser,
            idProduct = idProduct,
            content = content,
            user = user,
            date = date
        )
    }
}

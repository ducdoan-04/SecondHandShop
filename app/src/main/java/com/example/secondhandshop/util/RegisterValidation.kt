package com.example.secondhandshop.util


sealed class RegisterValidation (){
    object Success: RegisterValidation()
    data class Failed(val message: String):RegisterValidation()
}
data class RegisterFailedsState(
    val email: RegisterValidation,
    val password: RegisterValidation
)
package com.example.secondhandshop.util

sealed class Resource<T> (
    val data: T?=null,
    val message: String?=null,
//    val status: Int? = null
){
    class Success<T>(data: T):Resource<T>(data)
    class Error<T>(message: String): Resource<T>(message = message)
    class Loading<T>: Resource<T>()
    class Unspecified<T>: Resource<T>()

//    class Success<T>(data: T, status: Int) : Resource<T>(data, status = status)
//    class Error<T>(message: String,status: Int) : Resource<T>(message = message, status = status)

}
package com.example.secondhandshop.data


sealed class CategoryProducts(val category: String) {
    object Chair: Category("Chair")
    object Cupboard:Category("Cupboard")
    object Table:Category("Table")
    object Accessory:Category("Accessory")
    object Furniture:Category("Furniture")
}
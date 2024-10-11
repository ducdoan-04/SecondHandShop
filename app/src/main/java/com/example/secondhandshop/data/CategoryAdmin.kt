package com.example.secondhandshop.data

sealed class CategoryAdmin(val category: String) {
    object ManageAccounts: CategoryAdmin("Manage Accounts")
    object ManageProducts: CategoryAdmin("Manage Products")
    object ManageReports: CategoryAdmin("Manage Reports")
    object Statistical: CategoryAdmin("Statistical")
}
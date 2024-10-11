package com.example.secondhandshop.util

import android.view.View
import androidx.fragment.app.Fragment
import com.example.secondhandshop.R
import com.example.secondhandshop.activities.ShoppingActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Fragment.hideBottomNavigationView(){
    val bottomNavigationView = (activity as ShoppingActivity).findViewById<BottomNavigationView>(R.id.bottomNavgation)
    bottomNavigationView.visibility = View.GONE
}

fun Fragment.showBottomNavigationView(){
    val bottomNavigationView = (activity as ShoppingActivity).findViewById<BottomNavigationView>(R.id.bottomNavgation)
    bottomNavigationView.visibility = View.VISIBLE
}
package com.example.secondhandshop.fragments.shopping

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.admin.AdminHomeViewpagerAdapter
import com.example.secondhandshop.data.User

import com.example.secondhandshop.databinding.FragmentOrderManagementBinding
import com.example.secondhandshop.fragments.adminCategories.MainCategoryAdminFragment
import com.example.secondhandshop.fragments.adminCategories.ManageAccountsFragment
import com.example.secondhandshop.fragments.adminCategories.ManageProductsFragment
import com.example.secondhandshop.fragments.adminCategories.ManageReportsFragment
import com.example.secondhandshop.fragments.adminCategories.StatisticalFragment
import com.example.secondhandshop.fragments.statusOrder.CanceledFragment
import com.example.secondhandshop.fragments.statusOrder.ConfirmedFragment
import com.example.secondhandshop.fragments.statusOrder.DeliveredFragment
import com.example.secondhandshop.fragments.statusOrder.OrderedFragment
import com.example.secondhandshop.fragments.statusOrder.ReturnedFragment
import com.example.secondhandshop.fragments.statusOrder.ShippedFragment
import com.example.secondhandshop.util.Resource

import com.example.secondhandshop.viewmodel.statusOrder.ManinCategoryOrderManagementViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class OrderManagementFragment: Fragment(R.layout.fragment_order_management) {//Admin home fragment
    private lateinit var binding: FragmentOrderManagementBinding
    private val viewModel by viewModels<ManinCategoryOrderManagementViewModel> ()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderManagementBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showUserLoading()
                    }
                    is Resource.Success -> {
                        hideUserLoading()
                        showUserInformation(it.data!!)
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }


        val categoriesOrderManagementFragment = arrayListOf<Fragment>(
            OrderedFragment(),
            ConfirmedFragment(),
            ShippedFragment(),
            DeliveredFragment(),
            ReturnedFragment(),
            CanceledFragment()
        )

        binding.viewpagerHome.isUserInputEnabled = false

        val viewPaper2Adapter =
            AdminHomeViewpagerAdapter(categoriesOrderManagementFragment, childFragmentManager,lifecycle)
        binding.viewpagerHome.adapter = viewPaper2Adapter
        TabLayoutMediator(binding.tabLayout, binding.viewpagerHome){ tab, position ->
            when(position){
                0 -> tab.text = "Ordered"
                1 -> tab.text = "Confirmed"
                2 -> tab.text = "Shipped"
                3 -> tab.text = "Delivered"
                4 -> tab.text = "Returned"
                5 -> tab.text = "Canceled"
            }
        }.attach()

    }

    private fun showUserInformation(data: User) {
        binding.apply {
            Glide.with(this@OrderManagementFragment).load(data.imagePath).error(
                ColorDrawable(Color.BLACK)
            ).into(imageUser)
            binding.tvUserName.text = "${data.firstName} ${data.lastName}"
            binding.tvUserId.text = data.email

        }
    }


    private fun hideUserLoading() {
        binding.apply {
            ProductProgressBar.visibility = View.GONE
            imageUser.visibility = View.VISIBLE
            tvUserName.visibility = View.VISIBLE
        }
    }

    private fun showUserLoading() {
        binding.apply {
            ProductProgressBar.visibility = View.VISIBLE
            imageUser.visibility = View.INVISIBLE
            tvUserName.visibility = View.INVISIBLE
        }
    }
}
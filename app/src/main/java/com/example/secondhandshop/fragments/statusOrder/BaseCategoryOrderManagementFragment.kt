package com.example.secondhandshop.fragments.statusOrder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.BestProductAdapter
import com.example.secondhandshop.adapters.ordermanagement.ListOrderManagementAdapter
import com.example.secondhandshop.databinding.FragmentBaseCategoryBinding
import com.example.secondhandshop.databinding.FragmentOrderManagementBaseCategoryBinding
import com.example.secondhandshop.di.UserManager
import com.example.secondhandshop.fragments.shopping.OrderManagementFragmentDirections
import com.example.secondhandshop.util.showBottomNavigationView
import javax.inject.Inject

open class BaseCategoryOrderManagementFragment: Fragment(R.layout.fragment_order_management_base_category) {
    private lateinit var binding: FragmentOrderManagementBaseCategoryBinding
    protected val listOrderManagementAdapter: ListOrderManagementAdapter by lazy { ListOrderManagementAdapter() }

    @Inject
    lateinit var userManager: UserManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =FragmentOrderManagementBaseCategoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListOrderStatus()
        listOrderManagementAdapter.onClick ={
            val action = OrderManagementFragmentDirections.actionOrderManagementFragment2ToOrderManageOrderDetailFragment2(it)
            findNavController().navigate(action)
        }


    }

    fun showLoading(){
        binding.progressbarAllOrders.visibility = View.VISIBLE
    }
    fun hideLoading(){
        binding.progressbarAllOrders.visibility = View.GONE
    }



    open fun onOfferPagingRequest(){

    }
    open fun onBestProductsPagingRequest(){

    }

    private fun setupListOrderStatus() {
        binding.rvAllOrders.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = listOrderManagementAdapter
        }
    }



    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }

}
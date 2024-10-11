package com.example.secondhandshop.fragments.statusOrder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.ordermanagement.ListClientOrderAdapter
import com.example.secondhandshop.databinding.FragmentOrderManagementListOrdersBinding
import com.example.secondhandshop.di.UserManager
import com.example.secondhandshop.fragments.shopping.OrderManagementFragmentDirections
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.viewmodel.statusOrder.ManinCategoryOrderManagementViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

private val TAG = "OrderedFragment"
@AndroidEntryPoint
class OrderedFragment: Fragment(R.layout.fragment_order_management_list_orders) {//MainCategoryFragment
    private lateinit var binding: FragmentOrderManagementListOrdersBinding
    private lateinit var listClientOrderAdapter: ListClientOrderAdapter

    @Inject
    lateinit var userManager: UserManager

    private val viewModel by viewModels<ManinCategoryOrderManagementViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderManagementListOrdersBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListOrderManagement()

        val idShop = userManager.user?.email ?: ""
        viewModel.getListOrders(idShop,"Ordered")

        lifecycleScope.launchWhenStarted {
            viewModel.allOrders.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        listClientOrderAdapter.differ.submitList(it.data)
                        hideLoading()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        Log.e(TAG, it.message.toString())
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        listClientOrderAdapter.onClick={
            val action = OrderManagementFragmentDirections.actionOrderManagementFragment2ToOrderManageOrderDetailFragment2(it)
            findNavController().navigate(action)
        }

    }

    private fun hideLoading() {
        binding.progressbarAllOrders.visibility = View.GONE
    }

    private fun showLoading() {
        binding.progressbarAllOrders.visibility = View.VISIBLE
    }


    private fun setupListOrderManagement() {
        listClientOrderAdapter = ListClientOrderAdapter()
        binding.rvAllOrders.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = listClientOrderAdapter
        }
    }
}
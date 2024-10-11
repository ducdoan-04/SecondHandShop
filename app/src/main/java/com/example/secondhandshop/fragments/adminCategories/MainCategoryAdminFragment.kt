package com.example.secondhandshop.fragments.adminCategories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.admin.AccountHomeAdapter
import com.example.secondhandshop.adapters.admin.ProductHomeAdapter
import com.example.secondhandshop.databinding.AdminAccountRvItemBinding
import com.example.secondhandshop.databinding.FragmentAdminMainCategoryBinding
import com.example.secondhandshop.fragments.shopping.AdminHomeFragmentDirections
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.viewmodel.admin.MainCategoryAdminViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

private val TAG = "MainCategoryAdminFragment"
@AndroidEntryPoint
class MainCategoryAdminFragment: Fragment(R.layout.fragment_admin_main_category) {
    private lateinit var binding: FragmentAdminMainCategoryBinding
    private lateinit var accountHomeAdapter: AccountHomeAdapter
    private lateinit var productHomeAdapter: ProductHomeAdapter

    private val  viewModel by viewModels<MainCategoryAdminViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminMainCategoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAccountHomeRv()
        setupProductHomeRv()

        accountHomeAdapter.onSeeAccountClick ={email->
            if (email.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Error account not found", Toast.LENGTH_SHORT).show()
            } else {
            val action = AdminHomeFragmentDirections.actionAdminHomeFragmentToViewProfileShopFragment(email.toString())
            findNavController().navigate(action)
            }

        }

        productHomeAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product",it) }
            findNavController().navigate(R.id.action_adminHomeFragment_to_productDetailsFragment,b)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.homeAccounts.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        accountHomeAdapter.differ.submitList(it.data)
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

        lifecycleScope.launchWhenStarted {
            viewModel.homeProducts.collectLatest {
                when(it){
                    is Resource.Loading -> {
                       binding.ProductsProgressbar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        productHomeAdapter.differ.submitList(it.data)
                        binding.ProductsProgressbar.visibility = View.GONE
                    }
                    is Resource.Error -> {
                        Log.e(TAG, it.message.toString())
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.ProductsProgressbar.visibility = View.GONE
                    }
                    else -> Unit
                }
            }
        }
//        binding.nestedScrollMainCategory.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener{v,_,scrollY,_,_ ->
//            if (v.getChildAt(0).bottom <= v.height + scrollY ){
//                viewModel.fetchBestProducts()
//            }
//        })

        lifecycleScope.launchWhenStarted {
            viewModel.totalAccountCount.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        binding.tvquantityTotalAccount.text = "${it.data}"
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

        lifecycleScope.launchWhenStarted {
            viewModel.totalProductCount.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        binding.tvquantitytotalproducts.text = "${it.data}"
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

        lifecycleScope.launchWhenStarted {
            viewModel.totalReportCount.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        binding.tvquantitytotalReports.text = "${it.data}"
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
        var totalPrice = 0.0
        lifecycleScope.launchWhenStarted {
            viewModel.totalRevenue.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        totalPrice = it.data!!
                        val formattedTotalPrice = String.format("%.2f", totalPrice)
                        binding.tvquantitytotalRevenue.text = "$ ${formattedTotalPrice}"
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


    }

    private fun setupAccountHomeRv() {
        accountHomeAdapter = AccountHomeAdapter()
        binding.rvViewListAccount.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = accountHomeAdapter
        }

    }

    private fun setupProductHomeRv() {
        productHomeAdapter = ProductHomeAdapter()
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2,GridLayoutManager.VERTICAL, false)
            adapter = productHomeAdapter
        }

    }

    private fun hideLoading() {
        binding.mainCategoryProgressbar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.mainCategoryProgressbar.visibility = View.VISIBLE
    }
}
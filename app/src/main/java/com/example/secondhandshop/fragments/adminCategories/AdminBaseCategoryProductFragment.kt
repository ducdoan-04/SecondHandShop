package com.example.secondhandshop.fragments.adminCategories

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.admin.AdminManageProductsAdapter
import com.example.secondhandshop.adapters.admin.AdminManageProductsVerifiAdapter
import com.example.secondhandshop.databinding.FragmentAdminBaseCategoryProductsBinding


open class AdminBaseCategoryProductFragment: Fragment(R.layout.fragment_admin_base_category_products){
    lateinit var binding: FragmentAdminBaseCategoryProductsBinding
    protected val manageproductsAdapter: AdminManageProductsAdapter by lazy { AdminManageProductsAdapter() }
    protected val manageproductsverifiAdapter: AdminManageProductsVerifiAdapter by lazy { AdminManageProductsVerifiAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminBaseCategoryProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupManageProductRv()
        setupManageProductVerifiRv()

        manageproductsAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_adminHomeFragment_to_productDetailsFragment,b)
        }



        manageproductsverifiAdapter.onClick={
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_adminHomeFragment_to_productDetailsFragment,b)
        }



        binding.linearVerifiProduct.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_adminVerifiProductsUserFragment)
        }

    }

    private fun setupManageProductRv() {
        binding.rvListAccounts2.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL,false)
            adapter = manageproductsAdapter
        }
    }

    private fun setupManageProductVerifiRv(){
        binding.rvVerifiProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL,false)
            adapter = manageproductsverifiAdapter
        }
    }

    fun hideManageLoading() {
        binding.offerProductsProgressBar.visibility = View.GONE
    }

    fun showManageLoading() {
        binding.offerProductsProgressBar.visibility = View.VISIBLE
    }
}

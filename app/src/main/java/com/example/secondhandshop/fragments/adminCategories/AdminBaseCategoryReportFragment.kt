package com.example.secondhandshop.fragments.adminCategories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.admin.AdminManageReportsAdapter
import com.example.secondhandshop.databinding.FragmentAdminBaseCategoryReportsBinding

open class AdminBaseCategoryReportFragment: Fragment(R.layout.fragment_admin_base_category_reports) {
    private lateinit var binding: FragmentAdminBaseCategoryReportsBinding
    protected val managereportsAdapter: AdminManageReportsAdapter by lazy { AdminManageReportsAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminBaseCategoryReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupManageReportRv()

        managereportsAdapter.onSeeClick={
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_adminHomeFragment_to_productDetailsFragment,b)
        }
    }

    private fun setupManageReportRv() {
        binding.rvListReports.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = managereportsAdapter
        }
    }

    fun hideManageLoading() {
        binding.offerProductsProgressBar.visibility = View.GONE
    }

    fun showManageLoading() {
        binding.offerProductsProgressBar.visibility = View.VISIBLE
    }
}
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
import com.example.secondhandshop.databinding.FragmentAdminHomeBinding
import com.example.secondhandshop.fragments.adminCategories.MainCategoryAdminFragment
import com.example.secondhandshop.fragments.adminCategories.ManageAccountsFragment
import com.example.secondhandshop.fragments.adminCategories.ManageProductsFragment
import com.example.secondhandshop.fragments.adminCategories.ManageReportsFragment
import com.example.secondhandshop.fragments.adminCategories.StatisticalFragment
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.viewmodel.admin.MainCategoryAdminViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AdminHomeFragment: Fragment(R.layout.fragment_admin_home) {
    private lateinit var binding: FragmentAdminHomeBinding
    private val viewModel by viewModels<MainCategoryAdminViewModel> ()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminHomeBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.userAdmin.collectLatest {
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


        val categoriesAdminFragment = arrayListOf<Fragment>(
            MainCategoryAdminFragment(),
            ManageAccountsFragment(),
            ManageProductsFragment(),
            ManageReportsFragment(),
            StatisticalFragment()
        )

        binding.viewpagerHome.isUserInputEnabled = false

        val viewPaper2Adapter =
            AdminHomeViewpagerAdapter(categoriesAdminFragment, childFragmentManager,lifecycle)
        binding.viewpagerHome.adapter = viewPaper2Adapter
        TabLayoutMediator(binding.tabLayout, binding.viewpagerHome){ tab, position ->
            when(position){
                0 -> tab.text = "Main"
                1 -> tab.text = "Manage Accounts"
                2 -> tab.text = "Manage Products"
                3 -> tab.text = "Manage Reports"
                4 -> tab.text = "Statistic"
            }
        }.attach()

    }

    private fun showUserInformation(data: User) {
        binding.apply {
            Glide.with(this@AdminHomeFragment).load(data.imagePath).error(
                ColorDrawable(Color.BLACK)
            ).into(imageUser)
            binding.tvUserName.text = "${data.firstName} ${data.lastName}"
            binding.tvUserId.text = data.email
//            viewModel.getListVerifiProducts("0")
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
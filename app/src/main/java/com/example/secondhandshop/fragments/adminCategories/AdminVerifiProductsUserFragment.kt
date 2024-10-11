package com.example.secondhandshop.fragments.adminCategories

import android.app.AlertDialog
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.admin.AdminVerifiProductsUserAdapter
import com.example.secondhandshop.data.User
import com.example.secondhandshop.databinding.AdminVerifiProductsUserBinding
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.util.VerticalItemDecoration
import com.example.secondhandshop.viewmodel.admin.AdminVerifiProductsUserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class AdminVerifiProductsUserFragment: Fragment(R.layout.admin_verifi_products_user) {

    private lateinit var binding: AdminVerifiProductsUserBinding
    private val adminVerifiProductsUserAdapter by lazy { AdminVerifiProductsUserAdapter() }
    private val viewModel by viewModels<AdminVerifiProductsUserViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AdminVerifiProductsUserBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adminVerifiProductsUserAdapter.onClick ={
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_adminVerifiProductsUserFragment_to_productDetailsFragment,b)
        }

        adminVerifiProductsUserAdapter.onVerifiProductClick ={id->
            val id = id
            val alertDialog = AlertDialog.Builder(requireContext()).apply {
                setTitle("Verifi the product")
                setMessage("You want to confirm the shop's products?")
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton("Yes") { dialog, _ ->
                    viewModel.verifiProduct(id,"1")
                    dialog.dismiss()
                }
            }
            alertDialog.create()
            alertDialog.show()
        }


        lifecycleScope.launchWhenStarted {
            viewModel.verifyStatusProducts.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        hideLoading()
                        Toast.makeText(requireContext(),"The product has been successfully approved", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

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

//        verifiProduct
        lifecycleScope.launchWhenStarted {
            viewModel.verifiProduct.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.VerifiProductProgressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.VerifiProductProgressBar.visibility = View.INVISIBLE
                        if (it.data!!.isEmpty()){
                            showEmptyProducts()
                            hideOtherViews()
                        }else {
                            hideEmptyProducts()
                            showOtherViews()
                            adminVerifiProductsUserAdapter.differ.submitList(it.data)
                        }
                    }
                    is Resource.Error -> {
                        binding.VerifiProductProgressBar.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        setupListProducts()

    }

    private fun showUserInformation(data: User) {
        binding.apply {
            Glide.with(this@AdminVerifiProductsUserFragment).load(data.imagePath).error(ColorDrawable(Color.BLACK)).into(imageUser)
            binding.tvUserName.text = "${data.firstName} ${data.lastName}"
            binding.tvUserId.text = data.email
//            viewModel.getListVerifiProducts("0")
        }
    }


    private fun hideUserLoading() {
        binding.apply {
            VerifiProductProgressBar.visibility = View.GONE
            imageUser.visibility = View.VISIBLE
            tvUserName.visibility = View.VISIBLE
        }
    }

    private fun showUserLoading() {
        binding.apply {
            VerifiProductProgressBar.visibility = View.VISIBLE
            imageUser.visibility = View.INVISIBLE
            tvUserName.visibility = View.INVISIBLE
        }
    }

    private fun hideLoading() {
        binding.apply {
            VerifiProductProgressBar.visibility = View.GONE
        }
    }

    private fun showLoading() {
        binding.apply {
            VerifiProductProgressBar.visibility = View.VISIBLE
        }
    }

    private fun  showOtherViews() {
        binding.apply {
            rvVerifiProducts.visibility = View.VISIBLE
        }
    }


    private fun hideOtherViews() {
        binding.apply {
            rvVerifiProducts.visibility = View.GONE
        }
    }


    private fun hideEmptyProducts() {
        binding.apply {
            layoutEmpty.visibility = View.GONE
        }
    }

    private fun showEmptyProducts() {
        binding.apply {
            layoutEmpty.visibility = View.VISIBLE
        }
    }


    private fun setupListProducts() {
        binding.rvVerifiProducts.apply {
//            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
            layoutManager = GridLayoutManager(requireContext(),2,GridLayoutManager.VERTICAL,false)
            adapter = adminVerifiProductsUserAdapter
            addItemDecoration(VerticalItemDecoration())
        }
    }
}
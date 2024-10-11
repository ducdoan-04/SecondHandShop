package com.example.secondhandshop.fragments.shopping

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.ProductFavouriteAdapter
import com.example.secondhandshop.databinding.FragmentProductFavoriteBinding
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.util.VerticalItemDecoration
import com.example.secondhandshop.viewmodel.ProductFavouriteViewModel


import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class ProductFavouriteFragment: Fragment(R.layout.fragment_product_favorite) {
    private lateinit var binding: FragmentProductFavoriteBinding
    private val productFavouriteAdapter by lazy { ProductFavouriteAdapter() }
    private val viewModel by activityViewModels<ProductFavouriteViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductFavoriteBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFavouriteRv()

        productFavouriteAdapter.onProductClick = {
            val b = Bundle().apply { putParcelable("product", it.product) }
            findNavController().navigate(R.id.action_productFavouriteFragment_to_productDetailsFragment,b)
        }

        productFavouriteAdapter.onDeleteClick = { itemToDelete ->
            val alertDialog = AlertDialog.Builder(requireContext()).apply {
                setTitle("Delete item from favourite")
                setMessage("Do you want to delete this item from your favourite?")
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton("Yes") { dialog, _ ->
                    viewModel.deleteFavouriteProduct(itemToDelete)
                    dialog.dismiss()
                }
            }
            alertDialog.create()
            alertDialog.show()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.productFavourites.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbarSearch.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarSearch.visibility = View.INVISIBLE
                        if (it.data!!.isEmpty()){
                            showEmptyCart()
                            hideOtherViews()
                        }else {
                            hideEmptyCart()
                            showOtherViews()
                            productFavouriteAdapter.differ.submitList(it.data)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressbarSearch.visibility = View.INVISIBLE
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
                        binding.progressbarSearch.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarSearch.visibility = View.GONE
                        Glide.with(requireView()).load(it.data!!.imagePath).error(
                            ColorDrawable(
                                Color.BLACK)
                        ).into(binding.imageUser)
                        binding.tvUserName.text = "${it.data.firstName} ${it.data.lastName}"
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarSearch.visibility = View.GONE
                    }
                    else -> Unit
                }
            }
        }

    }

    private fun showOtherViews() {
        binding.apply {
           rvFavourite.visibility = View.VISIBLE
        }
    }

    private fun hideOtherViews() {
        binding.apply {
            rvFavourite.visibility = View.GONE

        }
    }

    private fun hideEmptyCart() {
        binding.apply {
            layoutFavouriteEmpty.visibility = View.GONE
        }
    }

    private fun showEmptyCart() {
        binding.apply {
            layoutFavouriteEmpty.visibility = View.VISIBLE
        }
    }

    private fun setupFavouriteRv() {
        binding.rvFavourite.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = productFavouriteAdapter
            addItemDecoration(VerticalItemDecoration())
        }
    }


}
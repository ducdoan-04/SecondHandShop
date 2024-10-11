package com.example.secondhandshop.fragments.settings

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.ListUpdateProductShopAdapter
import com.example.secondhandshop.adapters.ViewProfileShopAdapter
import com.example.secondhandshop.databinding.FragmentListProductUpdateBinding
import com.example.secondhandshop.databinding.FragmentViewProfileShopBinding
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.util.VerticalItemDecoration
import com.example.secondhandshop.viewmodel.ListUpdateProductViewModel
import com.example.secondhandshop.viewmodel.ProfileShopViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ListUpdateProductFragment: Fragment(R.layout.fragment_list_product_update) {
    private lateinit var binding: FragmentListProductUpdateBinding
    private val listUpdateProductAdapter by lazy { ListUpdateProductShopAdapter() }
    private val viewModel by viewModels<ListUpdateProductViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListProductUpdateBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idUser = arguments?.let { ViewProfileShopFragmentArgs.fromBundle(it).IdUser }
        idUser?.let {
            viewModel.getUser(it)
            viewModel.getSearchProducts(it)
        }
        setupSearchRv()

        listUpdateProductAdapter.onProductClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_listUpdateProductFragment_to_productDetailsFragment, b)
        }

        listUpdateProductAdapter.onUpdateClick ={product->
            val IdProduct = product.id
            val action = ListUpdateProductFragmentDirections.actionListUpdateProductFragmentToUpdateProductFragment(IdProduct)
            findNavController().navigate(action)
        }


        lifecycleScope.launchWhenStarted {
            viewModel.searchProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarSearch.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarSearch.visibility = View.INVISIBLE
                        if (it.data!!.isEmpty()) {
                            showEmptySearch()
                            hideOtherViews()
                        } else {
                            hideEmptySearch()
                            showOtherViews()
                            listUpdateProductAdapter.differ.submitList(it.data)
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
//                        binding.tvCity.text = "${it.data.status}"

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
            rvSearch.visibility = View.VISIBLE
        }
    }

    private fun hideOtherViews() {
        binding.apply {
            rvSearch.visibility = View.GONE
        }
    }

    private fun hideEmptySearch() {
        binding.apply {
            layoutSearchEmpty.visibility = View.GONE
        }
    }

    private fun showEmptySearch() {
        binding.apply {
            layoutSearchEmpty.visibility = View.VISIBLE
        }
    }

    private fun setupSearchRv() {
        binding.rvSearch.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = listUpdateProductAdapter
            addItemDecoration(VerticalItemDecoration())
        }
    }
}
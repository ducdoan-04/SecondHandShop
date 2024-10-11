package com.example.secondhandshop.fragments.shopping

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.SearchProductsAdapter
import com.example.secondhandshop.databinding.FragmentSearchproductsBinding
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.util.VerticalItemDecoration
import com.example.secondhandshop.viewmodel.SearchProductsViewModel
import kotlinx.coroutines.flow.collectLatest

class SearchProductsFragment : Fragment(R.layout.fragment_searchproducts) {
    private lateinit var binding: FragmentSearchproductsBinding
    private val searchAdapter by lazy { SearchProductsAdapter() }
    private val viewModel by activityViewModels<SearchProductsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchproductsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchRv()

        searchAdapter.onProductClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_searchFragment_to_productDetailsFragment, b)
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
                            searchAdapter.differ.submitList(it.data)
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

        setupSearchBar()
    }

    private fun setupSearchBar() {
        val searchEditText = binding.root.findViewById<EditText>(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.isNotEmpty()) {
                        viewModel.searchProductsByName(it.toString())
                    } else {
                        viewModel.getSearchProducts()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
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
            adapter = searchAdapter
            addItemDecoration(VerticalItemDecoration())
        }
    }
}

package com.example.secondhandshop.fragments

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.DeleteProductAdapter
import com.example.secondhandshop.data.User
import com.example.secondhandshop.databinding.FragmentDeleteProductBinding
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.util.VerticalItemDecoration
import com.example.secondhandshop.viewmodel.DeleteProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class DeleteProductFragment: Fragment(R.layout.fragment_delete_product) {

    private lateinit var binding:FragmentDeleteProductBinding
//    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>
    private val DeleteProductAdapter by lazy { DeleteProductAdapter()}
    private val viewModel by viewModels<DeleteProductViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeleteProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DeleteProductAdapter.onProductClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_deleteProductFragment_to_productDetailsFragment, b)
        }
        DeleteProductAdapter.onDeleteClick ={ itemToDelete ->
            val IdProduct = itemToDelete.id
            val alertDialog = AlertDialog.Builder(requireContext()).apply {
                setTitle("Delete item from shop")
                setMessage("Do you want to delete this item from your shop?")
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton("Yes") { dialog, _ ->
                    viewModel.deleteProduct(IdProduct)
                    dialog.dismiss()
                }
            }
            alertDialog.create()
            alertDialog.show()
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

        lifecycleScope.launchWhenStarted {
            viewModel.deleteProducts.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbarSearch.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarSearch.visibility = View.INVISIBLE
                        if (it.data!!.isEmpty()){
                            showEmptyProducts()
                            hideOtherViews()
                        }else {
                            hideEmptyProducts()
                            showOtherViews()
                            DeleteProductAdapter.differ.submitList(it.data)
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
        setupSearchRv()


    }

    private fun showUserInformation(data: User) {
        binding.apply {
            Glide.with(this@DeleteProductFragment).load(data.imagePath).error(ColorDrawable(Color.BLACK)).into(imageUser)
            binding.tvUserName.text = "${data.firstName} ${data.lastName}"
            binding.tvidShop.text = data.email
            viewModel.getListDeleteProducts(data.email)
        }
    }

    private fun hideUserLoading() {
        binding.apply {
            progressbarSearch.visibility = View.GONE
            imageUser.visibility = View.VISIBLE
            tvUserName.visibility = View.VISIBLE
        }

    }

    private fun showUserLoading() {
        binding.apply {
            progressbarSearch.visibility = View.VISIBLE
            imageUser.visibility = View.INVISIBLE
            tvUserName.visibility = View.INVISIBLE
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

    private fun hideEmptyProducts() {
        binding.apply {
            layoutSearchEmpty.visibility = View.GONE
        }
    }

    private fun showEmptyProducts() {
        binding.apply {
            layoutSearchEmpty.visibility = View.VISIBLE
        }
    }
    private fun setupSearchRv() {
        binding.rvSearch.apply {
           layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
           adapter = DeleteProductAdapter
           addItemDecoration(VerticalItemDecoration())
        }
    }

}

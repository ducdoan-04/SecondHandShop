package com.example.secondhandshop.fragments.shopping

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.ColorsAdapter
import com.example.secondhandshop.adapters.CommentProductAdapter
import com.example.secondhandshop.adapters.SizeAdapter
import com.example.secondhandshop.adapters.ViewPager2Images
import com.example.secondhandshop.data.Address
import com.example.secondhandshop.data.CartProduct
import com.example.secondhandshop.data.Comment
import com.example.secondhandshop.data.Favourite
import com.example.secondhandshop.data.Report
import com.example.secondhandshop.data.User
import com.example.secondhandshop.databinding.FragmentProductDetailsBinding
import com.example.secondhandshop.di.UserManager
import com.example.secondhandshop.fragments.settings.ManageMyShopFragmentDirections
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.util.hideBottomNavigationView
import com.example.secondhandshop.viewmodel.DetailsViewModel
import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class ProductDetailsFragment: Fragment() {
    private val args by navArgs<ProductDetailsFragmentArgs>()

    private lateinit var binding: FragmentProductDetailsBinding
    private val viewPagerAdapter by lazy { ViewPager2Images() }
    private val sizesAdapter by lazy { SizeAdapter() }
    private val colorsAdapter by lazy { ColorsAdapter() }
    private var selectedColor: Int? = null
    private var selectedSize: String? = null
    private val viewModel by viewModels<DetailsViewModel>()
    private var user2: User? = null
    private val productsStorage = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var commentAdapter: CommentProductAdapter

    @Inject
    lateinit var userManager: UserManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.addToFavourite.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbar.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), "Saved to favorites successfully", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        binding.progressbar.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.addToReport.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbar.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), "Reported product success", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        binding.progressbar.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hideBottomNavigationView()
        binding = FragmentProductDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product

        setupSizesRv()
        setupColorsRv()
        setupViewpager()
        setupCommentRv()




        binding.imgClose.setOnClickListener {
            findNavController().navigateUp()
        }

        sizesAdapter.onItemClick = {
            selectedSize = it
        }
        colorsAdapter.onItemClick = {
            selectedColor = it
        }



        binding.buttonAddToCart.setOnClickListener {
            val idShop = binding.tvIdUser.text.toString()
            viewModel.addUpdateProductInCart(CartProduct(product,1,idShop.toString(),selectedColor, selectedSize))
        }

        binding.constraintProfile.setOnClickListener{
            val IdUser = binding.tvIdUser.text.toString()
//            findNavController().navigate(R.id.action_manageMyShopFragment_to_viewProfileShopFragment, IdUser)
            val action = ProductDetailsFragmentDirections.actionProductDetailsFragmentToViewProfileShopFragment(IdUser)
            findNavController().navigate(action)
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
                        val idProduct = binding.tvIdProduct.text.trim().toString()
                        viewModel.getCommentByIdProduct(idProduct)
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.listComment.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showUserLoading()
                    }
                    is Resource.Success -> {
                        hideUserLoading()
                        commentAdapter.differ.submitList(it.data)
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.addComment.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbar.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), "Comment product success", Toast.LENGTH_SHORT).show()
                        binding.editTextComment.setText("")
                    }
                    is Resource.Error -> {
                        binding.progressbar.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.addToCart.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.buttonAddToCart.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.buttonAddToCart.revertAnimation()
                        Toast.makeText(requireContext(), "Product was added", Toast.LENGTH_SHORT).show()
                        binding.buttonAddToCart.setBackgroundColor(resources.getColor(R.color.black))
                    }

                    is Resource.Error -> {
                        binding.buttonAddToCart.stopAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        binding.apply {
            tvProductName.text = product.name
            tvProductPrice.text = "$ ${product.price}"
            tvProductDescription.text = product.description
            tvUserId.text = "2handshop.vn/${product.idUser}"
            tvIdUser.text=product.idUser

            viewModel.getUser(product.idUser.toString())

            val userEmail = userManager.user?.email
            tvIdUserAccount.text = userEmail ?: ""
            tvIdProduct.text = product.id

            if (product.colors.isNullOrEmpty())
                tvProductColor.visibility = View.INVISIBLE
            if (product.sizes.isNullOrEmpty())
                tvProductSize.visibility = View.INVISIBLE
        }

        viewPagerAdapter.differ.submitList(product.images)
        product.colors?.let { colorsAdapter.differ.submitList(it) }
        product.sizes?.let { sizesAdapter.differ.submitList(it) }


        binding.imageFavourite.setOnClickListener {
                val id =  UUID.randomUUID().toString()
                val idUser = userManager.user?.email ?: ""
                val idProduct = product.id

                val favourite = Favourite(product,id, idUser, idProduct)

                viewModel.addFavourite(favourite)
        }

        binding.imageReport.setOnClickListener {
            showReportDialog(product.id)

//            val id =  UUID.randomUUID().toString()
//            val idUser = userManager.user?.email ?: ""
//            val idProduct = product.id
//            val content = ""
//            val report = Report(id, idUser, idProduct,"")
//
//            viewModel.addReport(report)
        }

        binding.buttonPostComment.setOnClickListener {
            val id =  UUID.randomUUID().toString()
            val idUser = userManager.user?.email ?: ""
            val idProduct = product.id
            val content = binding.editTextComment.text.trim().toString()


            viewModel.getUserClient().observe(viewLifecycleOwner, Observer { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading indicator if necessary
                    }
                    is Resource.Success -> {
                        val user = resource.data
                        if (user != null) {
                            val comment = Comment(id, idUser, idProduct, content, user)
                            viewModel.addComment(comment)
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            })
        }

    }

    private fun showReportDialog(idProduct: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Report Product")
        val product = args.product
        val input = EditText(requireContext())
        input.hint = "Enter your report"
        builder.setView(input)

        builder.setPositiveButton("Submit") { dialog, which ->
            val id = UUID.randomUUID().toString()
            val idUser = userManager.user?.email ?: ""
            val content = input.text.toString()
            val report = Report(product,id, idUser, idProduct, content,"0")
            viewModel.addReport(report)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }


    private fun setupViewpager() {
        binding.apply {
            viewPagerProductImages.adapter = viewPagerAdapter
        }
    }

    private fun setupColorsRv() {
        binding.rvColors.apply {
            adapter = colorsAdapter
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        }
    }

    private fun setupSizesRv() {
        binding.rvSizes.apply {
            adapter = sizesAdapter
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        }
    }

    private fun showUserInformation(data: User) {
        binding.apply {
            Glide.with(this@ProductDetailsFragment).load(data.imagePath).error(ColorDrawable(Color.BLACK)).into(imageUser)
            tvUserName.setText(data.firstName +" "+ data.lastName)
        }
    }

    private fun hideUserLoading() {
        binding.apply {
            tvUserId.visibility = View.VISIBLE
            tvUserName.visibility = View.VISIBLE
            imageUser.visibility = View.VISIBLE
        }

    }

    private fun showUserLoading() {
        binding.apply {
            imageUser.visibility = View.INVISIBLE
            tvUserId.visibility = View.INVISIBLE
            tvUserName.visibility = View.INVISIBLE
        }

    }

    private fun setupCommentRv() {
        commentAdapter = CommentProductAdapter()
        binding.rvComment.apply {
            adapter = commentAdapter
            layoutManager= LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        }
    }



}
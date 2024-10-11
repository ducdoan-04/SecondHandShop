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
import com.bumptech.glide.Glide
import com.example.secondhandshop.R
import com.example.secondhandshop.databinding.FragmentManageMyShopBinding
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.util.showBottomNavigationView
import com.example.secondhandshop.viewmodel.ManageMyShopViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class ManageMyShopFragment: Fragment() {
    private lateinit var binding: FragmentManageMyShopBinding
    val viewModel by viewModels<ManageMyShopViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentManageMyShopBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.constraintProfile.setOnClickListener {
            val IdUser = binding.tvIdUser.text.toString()
//            findNavController().navigate(R.id.action_profileFragment_to_userAccountFragment)
            val action = ManageMyShopFragmentDirections.actionManageMyShopFragmentToViewProfileShopFragment(IdUser)
            findNavController().navigate(action)
        }

        binding.linearAddProduct.setOnClickListener {
            findNavController().navigate(R.id.action_manageMyShopFragment_to_addProductFragment)
        }

        binding.linearMyProducts.setOnClickListener {
            val IdUser = binding.tvIdUser.text.toString()
//            findNavController().navigate(R.id.action_profileFragment_to_userAccountFragment)
            val action = ManageMyShopFragmentDirections.actionManageMyShopFragmentToViewProfileShopFragment(IdUser)
            findNavController().navigate(action)
        }

        binding.linearUpdateProduct.setOnClickListener {
            val IdUser = binding.tvIdUser.text.toString()
//            findNavController().navigate(R.id.action_manageMyShopFragment_to_listUpdateProductFragment)
            val action = ManageMyShopFragmentDirections.actionManageMyShopFragmentToListUpdateProductFragment(IdUser)
            findNavController().navigate(action)
        }

        binding.linearDeleteProduct.setOnClickListener {
            findNavController().navigate(R.id.action_manageMyShopFragment_to_deleteProductFragment)
        }

        binding.linearEditAddress.setOnClickListener {
            findNavController().navigate(R.id.action_manageMyShopFragment_to_addressFragment)
        }
        binding.linearEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_manageMyShopFragment_to_userAccountFragment)
        }


        binding.tvViewShop.setOnClickListener{
            val IdUser = binding.tvIdUser.text.toString()
//            findNavController().navigate(R.id.action_manageMyShopFragment_to_viewProfileShopFragment, IdUser)
            val action = ManageMyShopFragmentDirections.actionManageMyShopFragmentToViewProfileShopFragment(IdUser)
            findNavController().navigate(action)
        }

        binding.linearOrderManagement.setOnClickListener {
            findNavController().navigate(R.id.action_manageMyShopFragment_to_orderManagementFragment2)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbarManageMyShop.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarManageMyShop.visibility = View.GONE
                        Glide.with(requireView()).load(it.data!!.imagePath).error(
                            ColorDrawable(
                                Color.BLACK)
                        ).into(binding.imageUser)
                        binding.tvUserName.text = "${it.data.firstName} ${it.data.lastName}"
                        binding.tvUserId.text = "2handshop.vn/${it.data.email}"
                        binding.tvIdUser.text ="${it.data.email}"
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarManageMyShop.visibility = View.GONE
                    }
                    else -> Unit
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()

        showBottomNavigationView()
    }


}
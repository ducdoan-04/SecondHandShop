package com.example.secondhandshop.fragments.shopping

import android.content.Intent
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
import com.example.secondhandshop.activities.LoginRegisterActivity
import com.example.secondhandshop.databinding.FragmentProfileBinding
import com.example.secondhandshop.di.UserManager
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.util.showBottomNavigationView
import com.example.secondhandshop.viewmodel.ProfileViewModel

//import com.example.secondhandshop.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment: Fragment() {
    private lateinit var binding: FragmentProfileBinding
    val viewModel by viewModels<ProfileViewModel>()

    @Inject
    lateinit var userManager: UserManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.constraintProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_userAccountFragment)
        }
        binding.linearAllOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_ordersFragment)
        }
        binding.linearBilling.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToBillingFragment(0f, emptyArray(), false)
            findNavController().navigate(action)
        }
        binding.linearLogOut.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireActivity(), LoginRegisterActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.linearMyShop.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_manageMyShopFragment)
        }

        binding.linearAddress.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_addressFragment)
        }
        binding.linearFavourite.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_productFavouriteFragment)
        }

        binding.linearAdmin.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_adminHomeFragment)
        }

        binding.tvVersion.text = "Version 1.0"

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbarSettings.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarSettings.visibility = View.GONE
                        Glide.with(requireView()).load(it.data!!.imagePath).error(ColorDrawable(Color.BLACK)).into(binding.imageUser)
                        binding.tvUserName.text = "${it.data.firstName} ${it.data.lastName}"
                        binding.tvActivityStatus.text = it.data.status

                        if (it.data.position.toString() == "1"){
                            binding.tvAdmin.visibility = View.VISIBLE
                            binding.linearManageAdmin.visibility = View.VISIBLE
                        }else{
                            binding.tvAdmin.visibility = View.GONE
                            binding.linearManageAdmin.visibility = View.GONE
                        }

                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarSettings.visibility = View.GONE
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
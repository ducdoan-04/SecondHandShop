package com.example.secondhandshop.fragments.adminCategories

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.admin.AdminManageAccountsAdapter
import com.example.secondhandshop.adapters.admin.AdminManageProductsAdapter
import com.example.secondhandshop.data.CategoryAdmin
import com.example.secondhandshop.data.Report
import com.example.secondhandshop.data.User
import com.example.secondhandshop.databinding.FragmentAdminBaseCategoryBinding
import com.example.secondhandshop.fragments.shopping.AdminHomeFragment
import com.example.secondhandshop.fragments.shopping.AdminHomeFragmentDirections
import com.example.secondhandshop.viewmodel.admin.CategoryAdminManageAccountsViewModel
import com.example.secondhandshop.viewmodel.factory.BaseCategoryAdminViewModelFactoryFactory
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import javax.inject.Inject


open class AdminBaseCategoryFragment: Fragment(R.layout.fragment_admin_base_category){

    lateinit var binding: FragmentAdminBaseCategoryBinding
    protected val manageaccountsAdapter: AdminManageAccountsAdapter by lazy { AdminManageAccountsAdapter() }
    protected val manageproductsAdapter: AdminManageProductsAdapter by lazy { AdminManageProductsAdapter() }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminBaseCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupManageAccountRv()

        manageaccountsAdapter.onSeeAccountClick={ email->
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Error account not found", Toast.LENGTH_SHORT).show()
            } else {
                val action = AdminHomeFragmentDirections.actionAdminHomeFragmentToViewProfileShopFragment(email)
                findNavController().navigate(action)
            }

        }
        manageaccountsAdapter.onUpdateAccountClick={email->
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Error account not found", Toast.LENGTH_SHORT).show()
            } else {
                val action = AdminHomeFragmentDirections.actionAdminHomeFragmentToAdminUpdateUserAccountFragment(email)
                findNavController().navigate(action)
            }

        }


    }


    private fun setupManageAccountRv() {
//        manageaccountsAdapter = AdminManageAccountsAdapter()
        binding.rvListAccounts2.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL,false)
            adapter = manageaccountsAdapter
        }

    }


    fun hideManageAccountLoading() {
        binding.offerProductsProgressBar.visibility = View.GONE
    }

    fun showManageAccountLoading() {
        binding.offerProductsProgressBar.visibility = View.VISIBLE
    }

    fun hideManageLoading() {
        binding.offerProductsProgressBar.visibility = View.GONE
    }

    fun showManageLoading() {
        binding.offerProductsProgressBar.visibility = View.VISIBLE
    }


}
package com.example.secondhandshop.fragments.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.secondhandshop.adapters.BillingProductsAdapter
import com.example.secondhandshop.data.order.OrderStatus
import com.example.secondhandshop.data.order.getOrderStatus
import com.example.secondhandshop.databinding.FragmentOrderDetailBinding
import com.example.secondhandshop.databinding.FragmentOrderManagementOrderDetailBinding
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.util.VerticalItemDecoration
import com.example.secondhandshop.viewmodel.statusOrder.OrderManagementVerifiOrderViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class OrderManageOrderDetailFragment: Fragment() {
    private lateinit var binding: FragmentOrderManagementOrderDetailBinding
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    private val args by navArgs<OrderDetailFragmentArgs>()
    private val viewModel by viewModels<OrderManagementVerifiOrderViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderManagementOrderDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val order = args.order

        setupOrderRv()

        binding.apply {
            tvOrderId.text = "Order #${order.orderId}"

            stepView.setSteps(
                mutableListOf(
                    OrderStatus.Ordered.status,
                    OrderStatus.Confirmed.status,
                    OrderStatus.Shipped.status,
                    OrderStatus.Delivered.status,

                )
            )

            val currentOrderState = when(getOrderStatus(order.orderStatus)){
                is OrderStatus.Ordered -> 0
                is OrderStatus.Confirmed -> 1
                is OrderStatus.Shipped -> 2
                is OrderStatus.Delivered -> 3
                else -> 0
            }

            stepView.go(currentOrderState, false)
            if (currentOrderState == 3){
                stepView.done(true)
            }

            tvFullName.text = order.address.fullName
            tvAddress.text = "${order.address.street}, ${order.address.state}, ${order.address.city}"
            tvPhoneNumber.text = order.address.phone
            tvuidClient.text = order.uidClient

            tvTotalPrice.text = "$ ${order.totalPrice}"

            buttonStatusOrder.text ="${order.orderStatus}"
            buttonTypePayment.text = order.typePayment

        }

        billingProductsAdapter.differ.submitList(order.products)

        binding.buttonStatusOrder.setOnClickListener{
            val idOrder = order.orderId
            val uidClient = order.uidClient
            val alertDialog = AlertDialog.Builder(requireContext()).apply {
                setTitle("Verifi the order")
                setMessage("You want to confirm the order?")
                setNegativeButton("Cancel order") { dialog, _ ->
                    viewModel.verifiOrder(idOrder, uidClient,"Canceled")
                    dialog.dismiss()
                }
                setPositiveButton("Confirm order") { dialog, _ ->
                    viewModel.verifiOrder(idOrder, uidClient,"Confirmed")
                    dialog.dismiss()
                }

            }
            alertDialog.create()
            alertDialog.show()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.verifyStatusOrder.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), "Update status order success", Toast.LENGTH_SHORT).show()
                        hideLoading()
                        findNavController().popBackStack()
                    }
                    is Resource.Error -> {
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                        hideLoading()
                    }
                    else -> Unit
                }
            }
        }
    }


    private fun setupOrderRv(){
        binding.rvProducts.apply {
            adapter = billingProductsAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalItemDecoration())
        }
    }

    fun hideLoading() {
        binding.ProgressBar.visibility = View.GONE
    }

    fun showLoading() {
        binding.ProgressBar.visibility = View.VISIBLE
    }
}
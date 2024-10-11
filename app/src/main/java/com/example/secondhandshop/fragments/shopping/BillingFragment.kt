package com.example.secondhandshop.fragments.shopping

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
import com.example.secondhandshop.R
import com.example.secondhandshop.adapters.AddressAdapter
import com.example.secondhandshop.adapters.BillingProductsAdapter
import com.example.secondhandshop.data.Address
import com.example.secondhandshop.data.CartProduct
import com.example.secondhandshop.data.order.Order
import com.example.secondhandshop.data.order.OrderStatus
import com.example.secondhandshop.databinding.FragmentBillingBinding
import com.example.secondhandshop.util.HorizontalItemDecoration
import com.example.secondhandshop.util.Resource
import com.example.secondhandshop.viewmodel.BillingViewModel
import com.example.secondhandshop.viewmodel.OrderViewModel
import com.google.android.material.snackbar.Snackbar
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.CreateOrder
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.OrderIntent
import com.paypal.checkout.createorder.UserAction
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.Amount
import com.paypal.checkout.order.AppContext
import com.paypal.checkout.order.OrderRequest
import com.paypal.checkout.order.PurchaseUnit
import com.paypal.pyplcheckout.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@AndroidEntryPoint
class BillingFragment: Fragment() {
    var TAG ="MyTag"
    private lateinit var binding: FragmentBillingBinding
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    private val billingViewModel by viewModels<BillingViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f


    private var selectedAddress: Address? = null
    private val orderViewModel by viewModels<OrderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        products = args.products.toList()
        totalPrice = args.totalPrice

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentBillingBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtonPaymentOnline()
        setupBillingProductsRv()
        setupAddressRv()

        if (!args.payment){
            binding.apply {
                buttonPlaceOrder.visibility = View.INVISIBLE
                paymentButtonContainer.visibility = View.INVISIBLE
                totalBoxContainer.visibility = View.INVISIBLE
                middleLine.visibility = View.INVISIBLE
                bottomLine.visibility = View.INVISIBLE
            }
        }

        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }

        addressAdapter.onClick = {
            selectedAddress = it
            if (!args.payment) {
                val b = Bundle().apply { putParcelable("address", selectedAddress) }
                findNavController().navigate(R.id.action_billingFragment_to_addressFragment, b)
            }
        }

        lifecycleScope.launchWhenStarted {
            billingViewModel.user.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.tvidClient.text = it.data?.email.toString()
                        binding.progressbarAddress.visibility = View.GONE

                    }
                    is Resource.Error -> {
                        binding.progressbarAddress.visibility = View.GONE
                        Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            billingViewModel.address.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        addressAdapter.differ.submitList(it.data)
                        binding.progressbarAddress.visibility = View.GONE
                    }
                    is Resource.Error -> {
                        binding.progressbarAddress.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            orderViewModel.order.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.buttonPlaceOrder.startAnimation()
                    }
                    is Resource.Success -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        findNavController().navigateUp()
                        Snackbar.make(requireView(),"Your order was placed", Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Error -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        billingProductsAdapter.differ.submitList(products)

        lifecycleScope.launchWhenStarted {
            billingViewModel.idShop.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        val idList = it.data
                        if (idList != null && idList.isNotEmpty()) {
                            binding.tvidShop.text = idList[0].idShop.toString()
                        } else {
                            binding.tvidShop.text = "Shop not found"
                        }
                        binding.progressbarAddress.visibility = View.GONE
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarAddress.visibility = View.GONE
                    }
                    else -> Unit
                }
            }
        }
        val formattedTotalPrice = String.format(Locale.US, "%.2f", totalPrice)
        binding.tvTotalPrice.text = "$ $formattedTotalPrice"

        binding.tvFormatTotalPrice.text = formattedTotalPrice
//        addressAdapter.onClick = {
//            selectedAddress = it
//        }

        binding.buttonPlaceOrder.setOnClickListener {
            if (selectedAddress == null){
                Toast.makeText(requireContext(), "Please select and address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showOrderConfirmationDialog()
        }
    }

    private fun showOrderConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Order items")
            setMessage("Do you want to order your cart items?")
            setNegativeButton("Cancel"){dialog,_ ->
                dialog.dismiss()
            }
            setPositiveButton("Yes"){dialog,_ ->
                val idShop = binding.tvidShop.text.trim()
                val idClient = binding.tvidClient.text.trim()
                val uidClient = orderViewModel.getUid()
                val formattedTotalPrice = String.format(Locale.US, "%.2f", totalPrice)
                val order = Order(
                    OrderStatus.Ordered.status,
                    formattedTotalPrice.toFloat(),
                    products,
                    selectedAddress!!,
                    idShop.toString(),
                    idClient.toString(),
                    uidClient.toString(),
                    "Cash"
                )
                orderViewModel.placeOrder(order)
                dialog.dismiss()
            }
        }
        alertDialog.create()
        alertDialog.show()
    }

    private fun setupAddressRv() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    private fun setupBillingProductsRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductsAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    private fun setupButtonPaymentOnline(){
        val formattedTotalPrice = String.format(Locale.US, "%.2f", totalPrice)
        binding.paymentButtonContainer.setup(
            createOrder =
            CreateOrder { createOrderActions ->
                val order =
                    OrderRequest(
                        intent = OrderIntent.CAPTURE,
                        appContext = AppContext(userAction = UserAction.PAY_NOW),
                        purchaseUnitList =
                        listOf(
                            PurchaseUnit(
                                amount =
                                Amount(currencyCode = CurrencyCode.USD, value = formattedTotalPrice)
                            )
                        )
                    )
                createOrderActions.create(order)
            },
            onApprove = OnApprove { approval ->
                Log.i(TAG, "OrderId: ${approval.data.orderId}")
                Toast.makeText(requireContext(), "Payment Approved and Captured", Toast.LENGTH_SHORT).show()
                placeOrderOnline()
            },
            onCancel = OnCancel{
                Log.d(TAG, "Buyer canceled the PayPal experience")
                Toast.makeText(requireContext(), "Payment Cancelled", Toast.LENGTH_SHORT).show()
            },
            onError = OnError{errorInfo ->
                Log.d(TAG, "Error: ${errorInfo.reason}")
                Toast.makeText(requireContext(), "Payment Error", Toast.LENGTH_SHORT).show()
            }

        )
    }

    private fun placeOrderOnline() {
        val idShop = binding.tvidShop.text.trim()
        val idClient = binding.tvidClient.text.trim()
        val uidClient = orderViewModel.getUid()
        val formattedTotalPrice = String.format(Locale.US, "%.2f", totalPrice)
        val order = Order(
            OrderStatus.Ordered.status,
            formattedTotalPrice.toFloat(),
            products,
            selectedAddress!!,
            idShop.toString(),
            idClient.toString(),
            uidClient.toString(),
            "Online"
        )
        orderViewModel.placeOrder(order)
    }
}
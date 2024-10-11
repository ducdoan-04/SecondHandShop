package com.example.secondhandshop.adapters.ordermanagement

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.secondhandshop.R
import com.example.secondhandshop.data.order.Order
import com.example.secondhandshop.data.order.OrderStatus
import com.example.secondhandshop.data.order.getOrderStatus
import com.example.secondhandshop.databinding.OrderManagementItemBinding

class ListOrderManagementAdapter: RecyclerView.Adapter<ListOrderManagementAdapter.ListClientOrderViewHolder> () {

    inner class ListClientOrderViewHolder(private val binding: OrderManagementItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.apply {
                tvOrderId.text = order.orderId.toString()
                tvOrderDate.text = order.date
                val resource = itemView.resources

                val colorDrawable = when (getOrderStatus(order.orderStatus)) {
                    is OrderStatus.Ordered -> {
                        ColorDrawable(resource.getColor(R.color.g_orange_yellow))
                    }

                    is OrderStatus.Confirmed -> {
                        ColorDrawable(resource.getColor(R.color.g_green))
                    }

                    is OrderStatus.Delivered -> {
                        ColorDrawable(resource.getColor(R.color.g_green))
                    }

                    is OrderStatus.Shipped -> {
                        ColorDrawable(resource.getColor(R.color.g_green))
                    }

                    is OrderStatus.Canceled -> {
                        ColorDrawable(resource.getColor(R.color.g_red))
                    }

                    is OrderStatus.Returned -> {
                        ColorDrawable(resource.getColor(R.color.g_red))
                    }
                }

                imageOrderState.setImageDrawable(colorDrawable)
            }
        }

    }
    private val diffUtil = object : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.products == newItem.products
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListClientOrderViewHolder {
        return ListClientOrderViewHolder(
            OrderManagementItemBinding.inflate(
                LayoutInflater.from(parent.context),parent,false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ListClientOrderViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.bind(order)

        holder.itemView.setOnClickListener {
            onClick?.invoke(order)
        }
    }

    var onClick : ((Order) -> Unit)? = null

}
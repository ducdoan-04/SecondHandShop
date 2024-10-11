package com.example.secondhandshop.adapters.admin

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.secondhandshop.data.Product
import com.example.secondhandshop.databinding.AdminManageProductVerifiRvItemBinding
import com.example.secondhandshop.helper.getProductPrice

class AdminManageProductsVerifiAdapter: RecyclerView.Adapter<AdminManageProductsVerifiAdapter.AdminManageProductsVerifiViewHolder>() {

    inner class AdminManageProductsVerifiViewHolder(val binding: AdminManageProductVerifiRvItemBinding): ViewHolder(binding.root){
        fun bind(productverifi: Product){
            binding.apply {
                val priceAfterOffer = productverifi.offerPercentage.getProductPrice(productverifi.price)
                tvNewPrice.text = "$ ${String.format("%.2f",priceAfterOffer)}"
                tvPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                if (productverifi.offerPercentage == null)
                    tvPrice.visibility = View.INVISIBLE
                Glide.with(itemView).load(productverifi.images[0]).into(imgProduct)
                tvPrice.text = "$ ${productverifi.price}"
                tvName.text = productverifi.name
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>(){
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdminManageProductsVerifiViewHolder {
        return AdminManageProductsVerifiViewHolder(
            AdminManageProductVerifiRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AdminManageProductsVerifiViewHolder, position: Int) {
        val productverifi = differ.currentList[position]
        holder.bind(productverifi)
        holder.itemView.setOnClickListener{
            onClick?.invoke(productverifi)
        }
    }

    var onClick: ((Product) -> Unit)? = null

}
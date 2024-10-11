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
import com.example.secondhandshop.databinding.AdminManageProductRvItemBinding
import com.example.secondhandshop.helper.getProductPrice

class AdminManageProductsAdapter: RecyclerView.Adapter<AdminManageProductsAdapter.AdminManageProductViewHolder>() {

    inner class AdminManageProductViewHolder( val binding: AdminManageProductRvItemBinding): ViewHolder(binding.root){
        fun bind (product: Product){
            binding.apply {
                val priceAfterOffer = product.offerPercentage.getProductPrice(product.price)
                tvNewPrice.text = "$ ${String.format("%.2f",priceAfterOffer)}"
                tvPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                if (product.offerPercentage == null)
                    tvPrice.visibility = View.INVISIBLE
                Glide.with(itemView).load(product.images[0]).into(imgProduct)
                tvPrice.text = "$ ${product.price}"
                tvName.text = product.name
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

    override fun onCreateViewHolder( parent: ViewGroup,viewType: Int ): AdminManageProductViewHolder {
        return AdminManageProductViewHolder(
            AdminManageProductRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AdminManageProductViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)
        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
        holder.binding.imageEditProduct.setOnClickListener {
            onUpdateClick?.invoke(product.id)
        }
        holder.binding.imageDeleteProduct.setOnClickListener {
            onDeleteClick?.invoke(product.id)
        }
        holder.binding.imageVerifi.setOnClickListener {
            onVerifiClick?.invoke(product.id)
        }
    }
    var onClick: ((Product) -> Unit)? = null
    var onUpdateClick: ((String)-> Unit)? = null
    var onDeleteClick: ((String)-> Unit)? = null
    var onVerifiClick: ((String)-> Unit)? = null


}
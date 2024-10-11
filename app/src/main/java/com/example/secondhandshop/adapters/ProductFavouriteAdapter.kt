package com.example.secondhandshop.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondhandshop.R
import com.example.secondhandshop.data.Favourite
import com.example.secondhandshop.databinding.ItemFavouriteProductsBinding
import com.example.secondhandshop.helper.getProductPrice

class ProductFavouriteAdapter:RecyclerView.Adapter<ProductFavouriteAdapter.ProductFavouriteViewHolder>() {

    inner class ProductFavouriteViewHolder(val binding: ItemFavouriteProductsBinding):
      RecyclerView.ViewHolder(binding.root){

                fun bind(productFavourite: Favourite){
                    binding.apply {
//                        Glide.with(itemView).load(productFavourite.product.images[0]).into(imageProduct)
                        if (productFavourite.product.images.isNotEmpty()) {
                            Glide.with(itemView).load(productFavourite.product.images[0]).into(imageProduct)
                        } else {
                            // Set a placeholder image or handle the case when there are no images
                            imageProduct.setImageResource(R.drawable.ic_empty_box) // Replace with your placeholder image
                        }
                        tvProductName.text =productFavourite.product.name

                        val priceAfterPercentage = productFavourite.product.offerPercentage.getProductPrice(productFavourite.product.price)
                        tvProductPrice.text =  "$ ${String.format("%.2f",priceAfterPercentage)}"
                    }
                }
      }

    private val diffCallback = object : DiffUtil.ItemCallback<Favourite>(){
        override fun areItemsTheSame(oldItem: Favourite, newItem: Favourite): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: Favourite, newItem: Favourite): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductFavouriteViewHolder {
        return ProductFavouriteViewHolder(
            ItemFavouriteProductsBinding.inflate(
                LayoutInflater.from(parent.context),parent,false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductFavouriteViewHolder, position: Int) {
        val productFavourite =differ.currentList[position]
        holder.bind(productFavourite)

        holder.itemView.setOnClickListener {
            onProductClick?.invoke(productFavourite)
        }

        holder.binding.imageDeleteFavourite.setOnClickListener {
            onDeleteClick?.invoke(productFavourite)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


    var onProductClick: ((Favourite) -> Unit)? = null
    var onDeleteClick: ((Favourite)-> Unit)? = null
}
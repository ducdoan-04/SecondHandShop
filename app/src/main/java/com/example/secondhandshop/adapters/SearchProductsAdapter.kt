package com.example.secondhandshop.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondhandshop.data.Product
import com.example.secondhandshop.databinding.SearchProductItemBinding
import com.example.secondhandshop.helper.getProductPrice

class SearchProductsAdapter: RecyclerView.Adapter<SearchProductsAdapter.SearchProductsViewHolder>() {

    inner class SearchProductsViewHolder(val binding: SearchProductItemBinding):
        RecyclerView.ViewHolder(binding.root){

        fun bind(searchProduct: Product){
            binding.apply {
                Glide.with(itemView).load(searchProduct.images[0]).into(imageSearchProduct)
                tvProductSearchName.text = searchProduct.name
                tvSearchProductQuantity.text = "0"  // Update this if you have product quantity info

                val priceAfterPercentage = searchProduct.offerPercentage.getProductPrice(searchProduct.price)
                tvProductSearchPrice.text = "\$ ${String.format("%.2f", priceAfterPercentage)}"
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
    var onProductClick: ((Product) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchProductsViewHolder {
        return SearchProductsViewHolder(
            SearchProductItemBinding.inflate(
                LayoutInflater.from(parent.context),parent,false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: SearchProductsViewHolder, position: Int) {
        val searchProduct = differ.currentList[position]
        holder.bind(searchProduct)

        holder.itemView.setOnClickListener{
            onProductClick?.invoke(searchProduct)
        }
    }
}

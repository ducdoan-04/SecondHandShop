package com.example.secondhandshop.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondhandshop.data.Product
import com.example.secondhandshop.databinding.ProfileShopProductItemBinding
import com.example.secondhandshop.helper.getProductPrice

class ViewProfileShopAdapter: RecyclerView.Adapter<ViewProfileShopAdapter.ViewProfileShopViewHolder>() {

    inner class ViewProfileShopViewHolder(val binding: ProfileShopProductItemBinding):
        RecyclerView.ViewHolder(binding.root){

        fun bind(viewProductShop: Product){
            binding.apply {
                Glide.with(itemView).load(viewProductShop.images[0]).into(imageSearchProduct)
                tvProductSearchName.text = viewProductShop.name
//                tvSearchProductQuantity.text = "0"
                val priceAfterPercentage = viewProductShop.offerPercentage.getProductPrice(viewProductShop.price)
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewProfileShopViewHolder {
        return ViewProfileShopViewHolder(
            ProfileShopProductItemBinding.inflate(
                LayoutInflater.from(parent.context),parent,false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewProfileShopViewHolder, position: Int) {
       val viewProfileProductShop = differ.currentList[position]
        holder.bind(viewProfileProductShop)

        holder.itemView.setOnClickListener{
            onProductClick?.invoke(viewProfileProductShop)
        }
    }


}
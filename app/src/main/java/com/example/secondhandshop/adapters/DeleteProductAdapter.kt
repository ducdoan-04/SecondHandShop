package com.example.secondhandshop.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondhandshop.data.Product
import com.example.secondhandshop.databinding.ItemListDeleteProductBinding
import com.example.secondhandshop.helper.getProductPrice

class DeleteProductAdapter:RecyclerView.Adapter<DeleteProductAdapter.DeleteProductViewHolder>() {
    inner class DeleteProductViewHolder(val binding: ItemListDeleteProductBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(viewProductShop: Product){
            binding.apply {
                Glide.with(itemView).load(viewProductShop.images[0]).into(imageSearchProduct)
                tvProductSearchName.text = viewProductShop.name
                tvIdProduct.text = viewProductShop.id
//                tvSearchProductQuantity.text = "0"
                val priceAfterPercentage = viewProductShop.offerPercentage.getProductPrice(viewProductShop.price)
                tvProductSearchPrice.text = "\$ ${String.format("%.2f", priceAfterPercentage)}"

//                imageSearchProductColor.setImageDrawable(ColorDrawable(viewProductShop.colors.toString()?: Color.TRANSPARENT))
//                tvSearchProductSize.text = viewProductShop.sizes.toString()?:"".also { imageSearchProductSize.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeleteProductAdapter.DeleteProductViewHolder {
        return DeleteProductViewHolder(
            ItemListDeleteProductBinding.inflate(
                LayoutInflater.from(parent.context),parent,false
            )
        )
    }


    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: DeleteProductViewHolder, position: Int) {
        val viewProfileProductShop = differ.currentList[position]
        holder.bind(viewProfileProductShop)

        holder.itemView.setOnClickListener{
            onProductClick?.invoke(viewProfileProductShop)
        }
        holder.binding.imageDeleteProduct.setOnClickListener {
            onDeleteClick?.invoke(viewProfileProductShop)
        }
    }

    var onProductClick: ((Product) -> Unit)? = null
    var onDeleteClick: ((Product)-> Unit)? = null
}
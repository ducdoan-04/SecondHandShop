package com.example.secondhandshop.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondhandshop.data.CartProduct
import com.example.secondhandshop.data.ShopHeader
import com.example.secondhandshop.databinding.CartProductItemBinding
import com.example.secondhandshop.databinding.ItemShopHeaderBinding
import com.example.secondhandshop.helper.getProductPrice

class CartProductAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val SHOP_HEADER = 0
    private val PRODUCT_ITEM = 1

    inner class ShopHeaderViewHolder(val binding: ItemShopHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(shopHeader: ShopHeader) {
            binding.tvShopName.text = "Shop: ${shopHeader.shopName}"
        }
    }


    inner class CartProductsViewHolder(val binding: CartProductItemBinding):
        RecyclerView.ViewHolder(binding.root){

        fun bind(cartProduct: CartProduct){
            binding.apply {
                Glide.with(itemView).load(cartProduct.product.images[0]).into(imageCartProduct)
                tvProductCartName.text = cartProduct.product.name
                tvCartProductQuantity.text = cartProduct.quantity.toString()

                val priceAfterPercentage = cartProduct.product.offerPercentage.getProductPrice(cartProduct.product.price)
                tvProductCartPrice.text =  "$ ${String.format("%.2f",priceAfterPercentage)}"

                imageCartProductColor.setImageDrawable(ColorDrawable(cartProduct.selectedColor?: Color.TRANSPARENT))
                tvCartProductSize.text = cartProduct.selectedSize?:"".also { imageCartProductSize.setImageDrawable(ColorDrawable(Color.TRANSPARENT)) }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Any>(){
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is CartProduct && newItem is CartProduct) {
                oldItem.product.id == newItem.product.id
            } else if (oldItem is ShopHeader && newItem is ShopHeader) {
                oldItem.shopName == newItem.shopName
            } else {
                false
            }
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SHOP_HEADER -> ShopHeaderViewHolder(ItemShopHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            PRODUCT_ITEM -> CartProductsViewHolder(CartProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = differ.currentList[position]
        when (holder) {
            is ShopHeaderViewHolder -> holder.bind(item as ShopHeader)
            is CartProductsViewHolder -> {
                val cartProduct = item as CartProduct
                holder.bind(cartProduct)

                holder.itemView.setOnClickListener {
                    onProductClick?.invoke(cartProduct)
                }

                holder.binding.imagePlus.setOnClickListener {
                    onPlusClick?.invoke(cartProduct)
                }

                holder.binding.imageMinus.setOnClickListener {
                    onMinusClick?.invoke(cartProduct)
                }

                holder.binding.imageDeleteCart.setOnClickListener {
                    onDeleteClick?.invoke(cartProduct)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is ShopHeader -> SHOP_HEADER
            is CartProduct -> PRODUCT_ITEM
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    var onProductClick: ((CartProduct) -> Unit)? = null
    var onPlusClick: ((CartProduct) -> Unit)? = null
    var onMinusClick: ((CartProduct) -> Unit)? = null
    var onDeleteClick: ((CartProduct)-> Unit)? = null
}

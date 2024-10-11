package com.example.secondhandshop.adapters


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.secondhandshop.R
import com.example.secondhandshop.data.Address
import com.example.secondhandshop.data.Comment
import com.example.secondhandshop.databinding.AddressRvItemBinding
import com.example.secondhandshop.databinding.CommentProductRvItemBinding

class CommentProductAdapter: Adapter<CommentProductAdapter.CommentProductViewHolder>() {
    inner class CommentProductViewHolder(val binding: CommentProductRvItemBinding): ViewHolder(binding.root){
        fun bind(comment: Comment) {
            binding.apply {
                Glide.with(itemView).load(comment.user?.imagePath).error(ColorDrawable(Color.BLACK)).into(imgAccount)
                tvAccountUserName.text = "${comment.user?.firstName} ${comment.user?.lastName}"
                tvAccountId.text = comment.user?.email
                contentComment.text = comment.content
                date.text = comment.date
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Comment>(){
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }


    }
    val differ = AsyncListDiffer(this, diffUtil)



    var selectedAddress = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentProductViewHolder {
        return CommentProductViewHolder(
            CommentProductRvItemBinding.inflate(
                LayoutInflater.from(parent.context), parent,false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CommentProductViewHolder, position: Int) {
        val comment = differ.currentList[position]
        holder.bind(comment)
    }


}
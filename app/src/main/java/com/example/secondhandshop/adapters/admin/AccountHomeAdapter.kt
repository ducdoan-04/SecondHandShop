package com.example.secondhandshop.adapters.admin

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.secondhandshop.data.Accounts
import com.example.secondhandshop.databinding.AdminAccountRvItemBinding

class AccountHomeAdapter: RecyclerView.Adapter<AccountHomeAdapter.AccountHomeViewHolder>() {

    inner class AccountHomeViewHolder (val binding: AdminAccountRvItemBinding): ViewHolder(binding.root){
        fun bind(account: Accounts){
            binding.apply {
                Glide.with(itemView).load(account.imagePath).error(ColorDrawable(Color.BLACK)).into(imgAccount)
                tvAccountUserName.text = "${account.firstName} ${account.lastName}"
                tvAccountId.text = account.email

            }

        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Accounts>(){
        override fun areItemsTheSame(oldItem: Accounts, newItem: Accounts): Boolean {
            return oldItem.email == newItem.email
        }

        override fun areContentsTheSame(oldItem: Accounts, newItem: Accounts): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountHomeViewHolder {
        return AccountHomeViewHolder(
            AdminAccountRvItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
       return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AccountHomeViewHolder, position: Int) {
        val account = differ.currentList[position]
        holder.bind(account)

        holder.itemView.setOnClickListener{
            onClick?.invoke(account)
        }
        holder.binding.btnSeeAccount.setOnClickListener {
            onSeeAccountClick?.invoke(account.email)
        }
    }

    var onClick: ((Accounts) -> Unit)? = null
    var onSeeAccountClick: ((String) -> Unit)? = null
//    var onSeeAccountClick: ((Accounts) -> Unit)? = null
}
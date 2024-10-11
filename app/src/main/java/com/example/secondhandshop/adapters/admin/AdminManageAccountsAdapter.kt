package com.example.secondhandshop.adapters.admin

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.secondhandshop.R
import com.example.secondhandshop.data.Accounts
import com.example.secondhandshop.databinding.AdminManageAccountRvItemBinding

class AdminManageAccountsAdapter: RecyclerView.Adapter<AdminManageAccountsAdapter.AdminManageAccountViewHolder>() {

    inner class AdminManageAccountViewHolder( val binding: AdminManageAccountRvItemBinding): ViewHolder(binding.root){
        fun bind(account: Accounts){
            binding.apply {
                Glide.with(itemView).load(account.imagePath).error(ColorDrawable(Color.BLACK)).into(imgAccount)
                tvAccountUserName.text =  "${account.firstName} ${account.lastName}"
                tvAccountId.text = account.email

                tvPosition.text = if(account.position == "1"){
                    "Position: Admin"
                }else{
                    "Position: Client"
                }
                val color = if (account.status == "1") R.color.g_green else R.color.g_red
                imageStatus.backgroundTintList = ContextCompat.getColorStateList(itemView.context, color)
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

    override fun onCreateViewHolder( parent: ViewGroup,viewType: Int ): AdminManageAccountViewHolder {
        return AdminManageAccountViewHolder(
            AdminManageAccountRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AdminManageAccountViewHolder, position: Int) {
        val account = differ.currentList[position]
        holder.bind(account)
        holder.itemView.setOnClickListener{
            onClick?.invoke(account)
        }
        holder.binding.btnSeeAccount.setOnClickListener {
            onSeeAccountClick?.invoke(account.email)
        }
        holder.binding.imageBanAccount.setOnClickListener {
            onVerifiAccountClick?.invoke(account.email)
        }
        holder.binding.imageDeleteAccount.setOnClickListener {
            onDeleteAccountClick?.invoke(account.email)
        }
        holder.binding.imageEditAccount.setOnClickListener {
            onUpdateAccountClick?.invoke(account.email)
        }

    }
    var onClick: ((Accounts) -> Unit)? = null
    var onSeeAccountClick: ((String) -> Unit)? = null
    var onVerifiAccountClick: ((String) -> Unit)? = null
    var onDeleteAccountClick: ((String) -> Unit)? = null
    var onUpdateAccountClick: ((String)-> Unit)? = null
}
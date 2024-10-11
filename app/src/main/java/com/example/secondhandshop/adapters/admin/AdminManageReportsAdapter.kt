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
import com.example.secondhandshop.data.Product
import com.example.secondhandshop.data.Report
import com.example.secondhandshop.databinding.AdminManageReportRvItemBinding

class AdminManageReportsAdapter: RecyclerView.Adapter<AdminManageReportsAdapter.AdminManageReportViewHolder>() {

    inner class AdminManageReportViewHolder(val binding: AdminManageReportRvItemBinding): ViewHolder(binding.root){
        fun bind(report: Report){
            binding.apply {
                tvProductName.text = report.product.name
                tvIdReport.text = report.id
                tvIdProduct.text = report.idProduct
                tvAccountIdReport.text = "User report: ${report.idUser}"
                tvContentReport.text = "Content: ${report.content}"
                Glide.with(itemView).load(report.product.images[0]).error(ColorDrawable(Color.BLACK)).into(imgProduct)
                val color = if (report.status == "1") R.color.g_green else R.color.g_red
                imageStatus.backgroundTintList = ContextCompat.getColorStateList(itemView.context, color)
            }
        }
    }
    private val diffCallback = object : DiffUtil.ItemCallback<Report>(){
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminManageReportViewHolder {
        return AdminManageReportViewHolder(
            AdminManageReportRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AdminManageReportViewHolder, position: Int) {
       val report = differ.currentList[position]
        holder.bind(report)
        holder.binding.btnSee.setOnClickListener{
            onSeeClick?.invoke(report.product)
        }
    }
    var onSeeClick: ((Product) -> Unit)? = null
}
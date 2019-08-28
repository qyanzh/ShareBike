package com.example.west2summer.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.west2summer.databinding.OrderListItemBinding
import com.example.west2summer.source.OrderRecord
import com.example.west2summer.source.User

class OrderAdapter(val clickListener: OrderRecordListener) :
    ListAdapter<OrderRecord, OrderAdapter.ViewHolder>(OrderRecordDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: OrderListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderRecord, clickListener: OrderRecordListener) {
            binding.order = item
            binding.clickListener = clickListener
            if (User.currentUser.value!!.id == item.ownerId) {
                binding.title.text = "借出"
                binding.id.text = item.userId.toString()
            } else {
                binding.title.text = "借入"
                binding.id.text = item.ownerId.toString()
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = OrderListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class OrderRecordDiffCallback : DiffUtil.ItemCallback<OrderRecord>() {

    override fun areItemsTheSame(oldItem: OrderRecord, newItem: OrderRecord): Boolean {
        return oldItem.id == newItem.id
    }


    override fun areContentsTheSame(oldItem: OrderRecord, newItem: OrderRecord): Boolean {
        return oldItem == newItem
    }


}

class OrderRecordListener(val clickListener: (order: OrderRecord) -> Unit) {
    fun onClick(order: OrderRecord) {
        clickListener(order)
    }
}


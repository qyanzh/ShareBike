package com.example.west2summer.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.west2summer.R
import com.example.west2summer.databinding.OrderListItemBinding
import com.example.west2summer.source.OrderRecord
import com.example.west2summer.source.User

class OrderAdapter(val context: Context, private val clickListener: OrderRecordListener) :
    ListAdapter<OrderRecord, OrderAdapter.ViewHolder>(OrderRecordDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(context, item, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: OrderListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(context: Context, item: OrderRecord, clickListener: OrderRecordListener) {
            binding.order = item
            binding.clickListener = clickListener
            binding.state.text = if (item.isFinished == 1) {
                context.getString(R.string.finished)
            } else {
                if (item.isUsed == 1) {
                    context.getString(R.string.using)
                } else {
                    context.getString(R.string.unconfirmed)
                }
            }
            if (User.currentUser.value!!.id == item.ownerId) {
                binding.title.text = context.getString(R.string.rent_out)
                binding.id.text = item.userId.toString()
            } else {
                binding.title.text = context.getString(R.string.rent_in)
                binding.id.text = item.ownerId.toString()
            }
            binding.startTime.visibility =
                if (item.startTime.isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            binding.endTime.visibility =
                if (item.endTime.isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
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


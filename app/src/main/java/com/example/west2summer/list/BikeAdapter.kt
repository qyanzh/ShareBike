package com.example.west2summer.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.west2summer.databinding.BikeListItemBinding
import com.example.west2summer.source.BikeInfo

class BikeAdapter(val context: Context, private val clickListener: BikeListener) :
    ListAdapter<BikeInfo, BikeAdapter.ViewHolder>(BikeDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(context, item, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: BikeListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(context: Context, item: BikeInfo, clickListener: BikeListener) {
            binding.bikeinfo = item
            binding.clickListener = clickListener

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = BikeListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class BikeDiffCallback : DiffUtil.ItemCallback<BikeInfo>() {

    override fun areItemsTheSame(oldItem: BikeInfo, newItem: BikeInfo): Boolean {
        return oldItem.id == newItem.id
    }


    override fun areContentsTheSame(oldItem: BikeInfo, newItem: BikeInfo): Boolean {
        return oldItem == newItem
    }

}

class BikeListener(val clickListener: (order: BikeInfo) -> Unit) {
    fun onClick(bike: BikeInfo) {
        clickListener(bike)
    }
}


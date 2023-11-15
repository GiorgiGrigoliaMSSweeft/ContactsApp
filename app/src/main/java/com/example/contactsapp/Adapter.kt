package com.example.contactsapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.databinding.RvItemBinding

class Adapter(
    private var items: List<Item>
): RecyclerView.Adapter<Adapter.ViewHolder>() {
    inner class ViewHolder(val binding: RvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RvItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            rvImage.setImageBitmap(items[position].image)
            rvName.text = items[position].name
            rvPhoneNumber.text = items[position].phoneNumber
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // Function to update the list using DiffUtil
    fun updateList(newItems: List<Item>) {
        val diffCallback = ItemDiffCallback()
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = items.size
            override fun getNewListSize(): Int = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return diffCallback.areItemsTheSame(items[oldItemPosition], newItems[newItemPosition])
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return diffCallback.areContentsTheSame(items[oldItemPosition], newItems[newItemPosition])
            }
        })

        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }
}
package com.example.contactsapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.databinding.RvItemBinding
import com.example.contactsapp.diffutils.ItemDiffCallback
import com.example.contactsapp.model.Item

class Adapter : ListAdapter<Item, Adapter.ViewHolder>(ItemDiffCallback()) {
    inner class ViewHolder(val binding: RvItemBinding) : RecyclerView.ViewHolder(binding.root)

    var onItemClick: ((Item) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RvItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            if (getItem(position).bitmapImage != null) {
                rvImage.setImageBitmap(getItem(position).bitmapImage)
                rvImage.visibility = View.VISIBLE
                rvStringImage.visibility = View.GONE
            } else {
                rvStringImage.text = getItem(position)?.name?.get(0)?.toString() ?: ""
                rvStringImage.visibility = View.VISIBLE
                rvImage.visibility = View.INVISIBLE
            }
            rvName.text = getItem(position).name
            rvPhoneNumber.text = getItem(position).phoneNumber
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(getItem(position))
        }
    }
}
package com.example.contactsapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.databinding.RvItemBinding

class Adapter(
    val items: List<Item>
): RecyclerView.Adapter<Adapter.ViewHolder>() {
    inner class ViewHolder(val binding: RvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RvItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            rvImage.setImageResource(items[position].image)
            rvName.text = items[position].name
            rvPhoneNumber.text = items[position].phoneNumber
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
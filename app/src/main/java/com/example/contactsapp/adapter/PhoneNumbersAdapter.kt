package com.example.contactsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.databinding.PhoneNumberRvItemBinding
import com.example.contactsapp.model.Item

class PhoneNumbersAdapter : ListAdapter<Item, PhoneNumbersAdapter.ViewHolder>(ItemDiffCallback()) {
    inner class ViewHolder(val binding: PhoneNumberRvItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PhoneNumberRvItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            if (item.additionalPhoneNumbers != null) {
                phoneNumber.text = item.additionalPhoneNumbers.joinToString(", ") { it.first }
                phoneNumberLabel.text = item.additionalPhoneNumbers.joinToString(", ") { it.second }
            }
        }
    }
}
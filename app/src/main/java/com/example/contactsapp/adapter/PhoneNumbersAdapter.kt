package com.example.contactsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.databinding.PhoneNumberRvItemBinding
import com.example.contactsapp.diffutils.PairDiffCallback

class PhoneNumbersAdapter : ListAdapter<Pair<String, String>, PhoneNumbersAdapter.ViewHolder>(
    PairDiffCallback()
) {
    inner class ViewHolder(val binding: PhoneNumberRvItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PhoneNumberRvItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            phoneNumber.text = item.first
            phoneNumberLabel.text = item.second
        }
    }
}
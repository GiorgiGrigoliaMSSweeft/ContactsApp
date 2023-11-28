package com.example.contactsapp.adapter

import androidx.recyclerview.widget.DiffUtil

class PairDiffCallback : DiffUtil.ItemCallback<Pair<String, String>>() {
    override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
        // Check if the pairs represent the same item.
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
        // Check if the contents of the pairs are the same.
        return oldItem.first == newItem.first && oldItem.second == newItem.second
    }
}

package com.example.contactsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.databinding.SignificantDateRvItemBinding
import com.example.contactsapp.diffutils.PairDiffCallback

class SignificantDatesAdapter : ListAdapter<Pair<String, String>, SignificantDatesAdapter.ViewHolder>(PairDiffCallback()) {
    inner class ViewHolder(val binding: SignificantDateRvItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = SignificantDateRvItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            significantDate.text = item.first
            significantDateLabel.text = item.second
        }
    }
}
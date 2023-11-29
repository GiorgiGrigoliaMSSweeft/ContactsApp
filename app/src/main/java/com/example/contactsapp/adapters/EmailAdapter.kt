package com.example.contactsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.databinding.EmailRvItemBinding
import com.example.contactsapp.diffutils.PairDiffCallback

class EmailAdapter : ListAdapter<Pair<String, String>, EmailAdapter.ViewHolder>(
    PairDiffCallback()
) {
    inner class ViewHolder(val binding: EmailRvItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = EmailRvItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            email.text = item.first
            emailLabel.text = item.second
        }
    }
}
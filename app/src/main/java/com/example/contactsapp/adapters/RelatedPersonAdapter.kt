package com.example.contactsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.databinding.RelatedPersonRvItemBinding
import com.example.contactsapp.diffutils.PairDiffCallback

class RelatedPersonAdapter : ListAdapter<Pair<String, String>, RelatedPersonAdapter.ViewHolder>(
    PairDiffCallback()
) {
    inner class ViewHolder(val binding: RelatedPersonRvItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RelatedPersonRvItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            relatedPerson.text = item.first
            relatedPersonLabel.text = item.second
        }
    }
}
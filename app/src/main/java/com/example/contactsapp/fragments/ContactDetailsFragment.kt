package com.example.contactsapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contactsapp.adapter.PhoneNumbersAdapter
import com.example.contactsapp.databinding.ContactDetailsFragmentBinding

class ContactDetailsFragment : Fragment() {
    private val binding by lazy { ContactDetailsFragmentBinding.inflate(layoutInflater) }
    private val adapter by lazy { PhoneNumbersAdapter() }
    private val args by navArgs<ContactDetailsFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.phoneNumberRvItems.adapter = adapter
        binding.phoneNumberRvItems.layoutManager = LinearLayoutManager(requireContext())
        adapter.submitList(args.selectedItem.additionalPhoneNumbers)
    }
}

package com.example.contactsapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contactsapp.adapters.EmailAdapter
import com.example.contactsapp.adapters.PhoneNumbersAdapter
import com.example.contactsapp.adapters.RelatedPersonAdapter
import com.example.contactsapp.adapters.SignificantDatesAdapter
import com.example.contactsapp.databinding.ContactDetailsFragmentBinding

class ContactDetailsFragment : Fragment() {
    private val binding by lazy { ContactDetailsFragmentBinding.inflate(layoutInflater) }
    private val phoneNumbersAdapter by lazy { PhoneNumbersAdapter() }
    private val emailAdapter by lazy { EmailAdapter() }
    private val significantDatesAdapter by lazy { SignificantDatesAdapter() }
    private val relatedPersonAdapter by lazy { RelatedPersonAdapter() }
    private val args by navArgs<ContactDetailsFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.phoneNumberRvItems.adapter = phoneNumbersAdapter
        binding.phoneNumberRvItems.layoutManager = LinearLayoutManager(requireContext())
        phoneNumbersAdapter.submitList(args.selectedItem.additionalPhoneNumbers)

        if (args.selectedItem.emailList?.isNotEmpty() == true) {
            binding.emailRvItems.adapter = emailAdapter
            binding.emailRvItems.layoutManager = LinearLayoutManager(requireContext())
            binding.emailTitle.visibility = View.VISIBLE
            emailAdapter.submitList(args.selectedItem.emailList)
        }

        if (args.selectedItem.significantDate?.isNotEmpty() == true) {
            binding.significantDateRvItems.adapter = significantDatesAdapter
            binding.significantDateRvItems.layoutManager = LinearLayoutManager(requireContext())
            binding.significantDateTitle.visibility = View.VISIBLE
            significantDatesAdapter.submitList(args.selectedItem.significantDate)
        }

        if (args.selectedItem.relatedPersonsList?.isNotEmpty() == true) {
            binding.relatedPersonRvItems.adapter = relatedPersonAdapter
            binding.relatedPersonRvItems.layoutManager = LinearLayoutManager(requireContext())
            binding.relatedPersonTitle.visibility = View.VISIBLE
            relatedPersonAdapter.submitList(args.selectedItem.relatedPersonsList)
        }

        if (args.selectedItem.company?.isNotBlank() == true) {
            binding.companyTitle.visibility = View.VISIBLE
            binding.companyName.visibility = View.VISIBLE
            binding.companyName.text = args.selectedItem.company
        }

        if (args.selectedItem.note?.isNotBlank() == true) {
            binding.noteTitle.visibility = View.VISIBLE
            binding.noteText.visibility = View.VISIBLE
            binding.noteText.text = args.selectedItem.note
        }
    }
}

package com.example.contactsapp.fragments

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.R
import com.example.contactsapp.adapters.ContactItemAdapter
import com.example.contactsapp.databinding.SearchAndResultsFragmentBinding
import com.example.contactsapp.extensions.hideKeyboard
import com.example.contactsapp.permission.PermissionUtils
import com.example.contactsapp.viewmodel.AppViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchAndResultsFragment : Fragment() {
    private val binding by lazy { SearchAndResultsFragmentBinding.inflate(layoutInflater) }
    private val adapter by lazy { ContactItemAdapter() }
    private val viewModel: AppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvItems.adapter = adapter
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())

        if (!PermissionUtils.isReadContactsPermissionGranted(requireContext())) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS))
                showPermissionDeniedSettingsMessage()
            else requestContactsPermission()
        }

        lifecycleScope.launch {
            viewModel.filterContacts().flowOn(Dispatchers.Default).collect { contactList ->
                withContext(Dispatchers.Main) {
                    adapter.submitList(contactList)

                    val userInputNotBlank = viewModel.uiState.value.userInput.isNotBlank()
                    if (PermissionUtils.isReadContactsPermissionGranted(requireContext()) && userInputNotBlank) {
                        if (contactList.isEmpty()) binding.noResultLayout.root.visibility =
                            View.VISIBLE
                        else binding.noResultLayout.root.visibility = View.GONE
                    } else binding.noResultLayout.root.visibility = View.GONE
                }
            }
        }


        binding.phoneNumberInput.addTextChangedListener {
            viewModel.updateUserInput(it.toString())
        }

        binding.searchAndResultsFragment.setOnClickListener {
            hideKeyboard()
            requireActivity().currentFocus?.clearFocus()
        }

        binding.rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard()
                    requireActivity().currentFocus?.clearFocus()
                }
            }
        })

        adapter.onItemClick = {
            val action =
                SearchAndResultsFragmentDirections.actionSearchAndResultsFragmentToContactDetailsFragment(
                    it
                )
            findNavController().navigate(action)
        }

        binding.addContactFab.setOnClickListener {
            val action =
                SearchAndResultsFragmentDirections.actionSearchAndResultsFragmentToAddContactFragment()
            findNavController().navigate(action)
        }
    }

    // Only place where contacts get loaded
    override fun onResume() {
        super.onResume()
        if (PermissionUtils.isReadContactsPermissionGranted(requireContext()) && adapter.currentList.isEmpty())
            lifecycleScope.launch { viewModel.loadContacts(requireContext()) }
    }

    private fun requestContactsPermission() {
        val requestContactsPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) showPermissionDeniedToast()
            }
        requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun showPermissionDeniedToast() {
        Toast.makeText(
            requireContext(),
            R.string.toast_warning_message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showPermissionDeniedSettingsMessage() {
        val message = getString(R.string.snacbkar_warning_message)
        Snackbar.make(requireView(), message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.go_to_settings_btn) {
                openAppSettings()
            }.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts(SCHEME, requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    companion object {
        private const val SCHEME = "package"
    }
}
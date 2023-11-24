package com.example.contactsapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.databinding.SearchAndResultsFragmentBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class SearchAndResultsFragment : Fragment() {
    private lateinit var binding: SearchAndResultsFragmentBinding
    private val viewModel: AppViewModel by viewModels()
    private lateinit var adapter: Adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SearchAndResultsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = Adapter()
        binding.rvItems.adapter = adapter
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())

        if (!PermissionUtils.isReadContactsPermissionGranted(requireContext())) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS))
                showPermissionDeniedSettingsMessage()
            else requestContactsPermission()
        } else viewModel.loadContacts(requireContext())

        lifecycleScope.launch {
            viewModel.filterContacts().collect { contactList ->
                adapter.submitList(contactList)

                if (PermissionUtils.isReadContactsPermissionGranted(requireContext())) {
                    if (contactList.isEmpty()) binding.noResultLayout.root.visibility = View.VISIBLE
                    else binding.noResultLayout.root.visibility = View.GONE
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
    }

    override fun onStart() {
        super.onStart()
        if (PermissionUtils.isReadContactsPermissionGranted(requireContext()) && adapter.currentList.isEmpty())
            viewModel.loadContacts(requireContext())
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }

    private fun requestContactsPermission() {
        val requestContactsPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) viewModel.loadContacts(requireContext())
                else showPermissionDeniedToast()
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
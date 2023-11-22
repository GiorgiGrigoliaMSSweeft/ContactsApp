package com.example.contactsapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: AppViewModel by viewModels()
    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = Adapter()
        binding.rvItems.adapter = adapter
        binding.rvItems.layoutManager = LinearLayoutManager(this)

        // Checks permission and handles each case accordingly
        if (!PermissionUtils.isReadContactsPermissionGranted(this)) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS))
                showPermissionDeniedSettingsMessage()
            else requestContactsPermission()
        } else viewModel.loadContacts(this)

        lifecycleScope.launch {
            viewModel.filterContacts().collect { contactList ->
                // Updates UI with the filtered contacts
                adapter.submitList(contactList)

                // Show or hide the image and message based on conditions
                if (PermissionUtils.isReadContactsPermissionGranted(this@MainActivity)) {
                    if (contactList.isEmpty()) binding.noResultLayout.root.visibility = View.VISIBLE
                    else binding.noResultLayout.root.visibility = View.GONE
                }
            }
        }


        binding.phoneNumberInput.addTextChangedListener {
            viewModel.updateUserInput(it.toString())
        }

        binding.mainLayout.setOnClickListener {
            hideKeyboard()
            currentFocus?.clearFocus()
        }

        // A scroll listener to handle focus change and keyboard dismissal
        binding.rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // Check if the RecyclerView is scrolling
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // Hide keyboard
                    hideKeyboard()

                    // Clear focus from any focused view
                    currentFocus?.clearFocus()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (PermissionUtils.isReadContactsPermissionGranted(this) && adapter.currentList.isEmpty())
            viewModel.loadContacts(this)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun requestContactsPermission() {
        val requestContactsPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) viewModel.loadContacts(this)
                else showPermissionDeniedToast()
            }
        requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun showPermissionDeniedToast() {
        Toast.makeText(
            this,
            R.string.toast_warning_message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showPermissionDeniedSettingsMessage() {
        val message = getString(R.string.snacbkar_warning_message)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.go_to_settings_btn) {
                // Opens app settings when the user clicks on the action button
                openAppSettings()
            }.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts(SCHEME, packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    companion object {
        private const val SCHEME = "package"
    }
}
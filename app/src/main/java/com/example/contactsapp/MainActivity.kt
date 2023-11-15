package com.example.contactsapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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

        adapter = Adapter(emptyList())
        binding.rvItems.adapter = adapter
        binding.rvItems.layoutManager = LinearLayoutManager(this@MainActivity)

        // Checks permission and handles each case accordingly
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS))
                showPermissionDeniedSettingsMessage()
            else requestContactsPermission()
        } else viewModel.loadContacts(this)

        lifecycleScope.launch {
            viewModel.uiState.collect {
                // Updates adapter with the new data
                adapter.updateList(
                    if (viewModel.uiState.value.userInput.isNotBlank()) {
                        it.retrievedContacts.filter { item ->
                            item.phoneNumber.contains(viewModel.uiState.value.userInput.trim().replace(Regex("\\D"), ""))
                        }
                    } else {
                        it.retrievedContacts
                    }
                )
            }
        }

        binding.phoneNumberInput.addTextChangedListener {
            viewModel.updateUserInput(it.toString())
        }
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
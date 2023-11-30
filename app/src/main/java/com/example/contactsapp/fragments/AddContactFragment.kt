package com.example.contactsapp.fragments

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.contactsapp.databinding.AddContactFragmentBinding
import com.example.contactsapp.extensions.hideKeyboard
import com.google.android.material.snackbar.Snackbar


class AddContactFragment : Fragment() {
    private val binding by lazy { AddContactFragmentBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addContactFragment.setOnClickListener {
            hideKeyboard()
            requireActivity().currentFocus?.clearFocus()
        }

        binding.addPhotoButton.setOnClickListener {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                (ContextCompat.checkSelfPermission(
                    requireContext(),
                    READ_MEDIA_IMAGES
                ) == PERMISSION_GRANTED)
            ) {
                // Full access on Android 13 (API level 33) or higher
                pickImage.launch("image/*")

            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    READ_MEDIA_VISUAL_USER_SELECTED
                ) == PERMISSION_GRANTED
            ) {
                // Partial access on Android 14 (API level 34) or higher
                pickImage.launch("image/*")

            } else if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    READ_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED
            ) {
                // Full access up to Android 12 (API level 32)
                pickImage.launch("image/*")

            } else {
                // Access denied
                if (shouldShowRequestPermissionRationale(READ_MEDIA_IMAGES) ||
                    shouldShowRequestPermissionRationale(READ_MEDIA_VISUAL_USER_SELECTED) ||
                    shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)
                ) {
                    showPermissionDeniedSettingsMessage()
                } else requestPermission()
            }
        }
    }

    // Register ActivityResult handler
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val isGranted = results.any { it.value }
            if (isGranted) pickImage.launch("image/*")
        }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPermissions.launch(
                arrayOf(
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VISUAL_USER_SELECTED
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES))
        } else {
            requestPermissions.launch(arrayOf(READ_EXTERNAL_STORAGE))
        }
    }

    private fun showPermissionDeniedSettingsMessage() {
        val message = "To upload an image, app needs access to your library."
        Snackbar.make(requireView(), message, Snackbar.ANIMATION_MODE_SLIDE)
            .setAction("Go to settings") {
                openAppSettings()
            }.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri =
            Uri.fromParts(SearchAndResultsFragment.SCHEME, requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }


    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            // Handle the picked image URI, you can set it to ImageView or do whatever you need
            binding.photo.setImageURI(uri)
        }
}

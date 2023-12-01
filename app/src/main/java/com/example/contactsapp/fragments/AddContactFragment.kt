package com.example.contactsapp.fragments

import android.content.ContentProviderOperation
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.contactsapp.R
import com.example.contactsapp.databinding.AddContactFragmentBinding
import com.example.contactsapp.extensions.hideKeyboard
import com.example.contactsapp.permission.PermissionUtils
import java.io.IOException
import java.io.InputStream

class AddContactFragment : Fragment() {
    private val binding by lazy { AddContactFragmentBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addContactFragment.setOnClickListener {
            hideKeyboard()
            requireActivity().currentFocus?.clearFocus()
        }

        binding.addPhotoButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.saveButton.setOnClickListener {
            if (PermissionUtils.isWriteContactsPermissionGranted(requireContext())) saveContact()
            else requestPermissionLauncher.launch(android.Manifest.permission.WRITE_CONTACTS)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, save the contact
                saveContact()
            } else {
                // Permission denied
                Toast.makeText(requireContext(),
                    getString(R.string.perm_denied_not_saved),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun saveContact() {
        val firstName = binding.addFirstNameInput.text.toString().trim()
        val lastName = binding.lastNameInput.text.toString().trim()
        val number = binding.addNumberInput.text.toString().trim()
        val photoUri = binding.photo.tag as? Uri

        val cpo = ArrayList<ContentProviderOperation>()

        // Adding contact id
        cpo.add(ContentProviderOperation.newInsert(
            ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build())

        // Adding first and last names
        cpo.add(ContentProviderOperation.newInsert(
            ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0) // Use 0 to refer to the first operation
            .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
            .build()
        )

        // Adding phone number (mobile)
        cpo.add(ContentProviderOperation.newInsert(
            ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0) // Use 0 to refer to the first operation
            .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            .build()
        )

        // Adding image
        if (photoUri != null) {
            cpo.add(ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0) // Use 0 to refer to the first operation
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, inputStreamFromUri(photoUri))
                .build()
            )
        }

        try {
            // Applying the batch
            requireContext().contentResolver.applyBatch(ContactsContract.AUTHORITY, cpo)
            Log.d("TAG", "Contact saved")
        } catch (e: Exception) {
            Log.e("TAG", "Saving contact failed: ${e.message}")
        }
    }


    // Helper function to convert URI to InputStream
    private fun inputStreamFromUri(uri: Uri): ByteArray? {
        var inputStream: InputStream? = null
        try {
            inputStream = requireContext().contentResolver.openInputStream(uri)
            return inputStream?.readBytes()
        } catch (e: IOException) {
            // Handle the exception
            Log.e("TAG", "Error reading bytes from URI: ${e.message}")
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                // Handle the exception
                Log.e("TAG", "Error closing InputStream: ${e.message}")
            }
        }
        return null
    }


    // Registers a photo picker activity launcher in single-select mode.
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                binding.photo.setImageURI(uri)
                binding.photo.tag = uri
            }
            else Toast.makeText(
                requireContext(),
                getString(R.string.image_was_not_selected),
                Toast.LENGTH_LONG
            ).show()
        }
}

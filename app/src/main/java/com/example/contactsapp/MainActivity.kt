package com.example.contactsapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contactsapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS))
                showPermissionDeniedSettingsMessage()
            else requestContactsPermission()
        } else loadContacts()
    }

    private fun requestContactsPermission() {
        val requestContactsPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted)
                    loadContacts()
                else
                    showPermissionDeniedToast()
            }

        requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun showPermissionDeniedToast() {
        Toast.makeText(
            this,
            "Without access to contacts, items can't be synced.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showPermissionDeniedSettingsMessage() {
        val message = "This app needs access to contacts. Please grant access in the app settings."
        Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE)
            .setAction("Go to Settings") {
                // Open app settings when the user clicks on the action button
                openAppSettings()
            }.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun loadContacts() {
        runOnUiThread {
            // Query the contacts
            val contacts = getContacts()

            // Create an adapter and set it to the RecyclerView
            val adapter = Adapter(contacts)
            binding.rvItems.adapter = adapter
            binding.rvItems.layoutManager = LinearLayoutManager(this)
        }
    }

    @SuppressLint("Range")
    private fun getContacts(): List<Item> {
        val contactsList = mutableListOf<Item>()

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use { cursor1 ->
            while (cursor1.moveToNext()) {
                val contactId =
                    cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID))

                val name =
                    cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                val contactUri =
                    ContentUris.withAppendedId(
                        ContactsContract.Contacts.CONTENT_URI,
                        contactId.toLong()
                    )

                // Query contact photo
                val photoStream = ContactsContract.Contacts.openContactPhotoInputStream(
                    contentResolver,
                    contactUri
                )

                val contactPhoto = if (photoStream != null) {
                    BitmapFactory.decodeStream(photoStream)
                } else {
                    // If no photo is available, create a bitmap with the first letter of the name
                    createTextBitmap(name[0].toString())
                }

                // Query phone numbers
                val phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(contactId),
                    null
                )

                phoneCursor?.use { phoneCursorInner ->
                    while (phoneCursorInner.moveToNext()) {
                        val phoneNumber =
                            phoneCursorInner.getString(
                                phoneCursorInner.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                )
                            )
                        // You can customize how you handle the contact data here
                        contactsList.add(Item(contactPhoto, name, phoneNumber))
                    }
                }
                phoneCursor?.close()
            }
        }

        return contactsList
    }
}

private fun createTextBitmap(text: String): Bitmap {
    val paint = Paint().apply {
        color = Color.WHITE
        textSize = 50f
        isFakeBoldText = true
    }

    val imageSize = 100
    val strokeWidth = 5f
    val bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Draws a filled circle with gray stroke
    paint.color = Color.WHITE
    canvas.drawCircle(imageSize / 2f, imageSize / 2f, imageSize / 2f - strokeWidth / 2, paint)

    // Draws a gray box stroke around the circle
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = strokeWidth
    paint.color = Color.parseColor("#F0F0F1")
    canvas.drawCircle(imageSize / 2f, imageSize / 2f, imageSize / 2f - strokeWidth / 2, paint)

    // Draws the text in the center of the circle
    paint.style = Paint.Style.FILL
    paint.color = Color.BLACK
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText(text, imageSize / 2f, imageSize / 2f - (paint.descent() + paint.ascent()) / 2, paint)

    return bitmap
}
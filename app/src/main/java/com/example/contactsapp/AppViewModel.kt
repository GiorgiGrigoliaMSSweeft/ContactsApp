package com.example.contactsapp

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppUiState(
    val userInput: String = "",
    val retrievedContacts: List<Item> = emptyList()
)

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun updateUserInput(input: String) {
        _uiState.update {
            it.copy(
                userInput = input
            )
        }
    }

    fun loadContacts(context: Context) {
        _uiState.update {
            it.copy(
                retrievedContacts = getContacts(context)
            )
        }
    }

    @SuppressLint("Range")
    private fun getContacts(context: Context): List<Item> {
        val contactsList = mutableListOf<Item>()
        val cursor = context.contentResolver.query(
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

                // Queries contact photo
                val photoStream = ContactsContract.Contacts.openContactPhotoInputStream(
                    context.contentResolver,
                    contactUri
                )

                val contactPhoto = if (photoStream != null)
                    BitmapFactory.decodeStream(photoStream)
                else createTextBitmap(name[0].toString()) // If no photo is available, creates a bitmap with the first letter of the name

                // Query phone numbers
                val phoneCursor = context.contentResolver.query(
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
                        contactsList.add(Item(image = contactPhoto, name = name, phoneNumber = phoneNumber.replace(Regex("\\D"), "").replace(" ", "")))
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
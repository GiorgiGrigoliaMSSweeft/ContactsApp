package com.example.contactsapp.viewmodel

import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import com.example.contactsapp.model.Item
import com.example.contactsapp.uistate.AppUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())

    fun updateUserInput(input: String) {
        _uiState.update {
            it.copy(
                userInput = input
            )
        }
    }

    @OptIn(FlowPreview::class)
    fun filterContacts(): Flow<List<Item>> = _uiState.debounce(DEBOUNCE_TIMEOUT_MILLIS).map {
        if (it.userInput.isNotBlank()) {
            val inputAsNumber = it.userInput.trim().toIntOrNull()
            it.retrievedContacts.filter { item ->
                if (inputAsNumber != null)
                    item.phoneNumber.contains(it.userInput.replace("\\s".toRegex(), ""))
                else
                    item.name.contains(it.userInput.trim(), ignoreCase = true)
            }

        } else it.retrievedContacts
    }

    suspend fun loadContacts(context: Context) {
        _uiState.update {
            it.copy(
                retrievedContacts = getContacts(context)
            )
        }
    }

    private suspend fun getContacts(context: Context): List<Item> = withContext(Dispatchers.IO) {
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
                val contactIdColumnIndex =
                    cursor1.getColumnIndex(ContactsContract.Contacts._ID)

                if (contactIdColumnIndex != -1) {
                    val contactId = cursor1.getString(contactIdColumnIndex)

                    val nameColumnIndex =
                        cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)

                    if (nameColumnIndex != -1) {
                        val name = cursor1.getString(nameColumnIndex)

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

                        val contactPhoto = BitmapFactory.decodeStream(photoStream)

                        // Queries phone numbers
                        val phoneCursor = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )

                        phoneCursor?.use { phoneCursorInner ->
                            val phoneNumberColumnIndex =
                                phoneCursorInner.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                            while (phoneCursorInner.moveToNext() && phoneNumberColumnIndex != -1) {
                                val phoneNumber = phoneCursorInner.getString(phoneNumberColumnIndex)
                                contactsList.add(
                                    Item(
                                        bitmapImage = contactPhoto,
                                        name = name,
                                        phoneNumber = phoneNumber.replace(Regex("\\D"), "")
                                            .replace(" ", "")
                                    )
                                )
                            }
                        }
                        phoneCursor?.close()
                    }
                }
            }
        }

        return@withContext contactsList
    }


    companion object {
        private const val DEBOUNCE_TIMEOUT_MILLIS = 300L
    }
}
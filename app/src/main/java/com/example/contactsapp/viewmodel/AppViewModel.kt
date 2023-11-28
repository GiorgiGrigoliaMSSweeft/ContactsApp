package com.example.contactsapp.viewmodel

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
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
                    item.phoneNumber?.contains(it.userInput.replace("\\s".toRegex(), "")) ?: false
                else
                    item.name?.contains(it.userInput.trim(), ignoreCase = true) ?: false
            }

        }
        else it.retrievedContacts
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
        val processedContactIds = HashSet<String>() // Keeps track of processed contact IDs

        // Querying the contacts
        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val contactIdColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)

                if (contactIdColumnIndex != -1) {
                    val contactId = cursor.getString(contactIdColumnIndex)

                    // Check if the contact is already processed
                    if (!processedContactIds.contains(contactId)) {
                        val name = getContactName(context, contactId)
                        val contactPhoto = getContactImage(context, contactId)
                        val phoneNumbers = getPhoneNumbers(context, contactId)
                        val emailList = getEmailWithLabel(context, contactId)
                        val significantDate = getSignificantDateWithLabel(context, contactId)
                        val company = getCompany(context, contactId)
                        val relatedPersonsList = getRelatedPersons(context, contactId)
                        val note = getNote(context, contactId)

                        // Adding contact to the list
                        contactsList.add(
                            Item(
                                bitmapImage = contactPhoto,
                                name = name,
                                phoneNumber = phoneNumbers.first().first,
                                additionalPhoneNumbers = phoneNumbers,
                                emailList = emailList,
                                significantDate = significantDate,
                                company = company,
                                relatedPersonsList = relatedPersonsList,
                                note = note
                            )
                        )
                        // Add the contact ID to the processed set
                        processedContactIds.add(contactId)
                    }
                }
            }
        }

        return@withContext contactsList
    }

    private fun getPhoneNumbers(
        context: Context,
        contactId: String
    ): List<Pair<String, String>> {
        val phoneNumbersList = mutableListOf<Pair<String, String>>()
        val phoneCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        phoneCursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val phoneNumberColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val labelColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)
                val typeColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)

                if (phoneNumberColumnIndex != -1) {
                    val phoneNumber = cursor.getString(phoneNumberColumnIndex)
                        .replace(Regex("\\D"), "")
                        .replace(" ", "")

                    val label = if (labelColumnIndex != -1) {
                        val type = cursor.getInt(typeColumnIndex)
                        val labelResource = cursor.getString(labelColumnIndex)

                        if (type == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
                            labelResource
                        else {
                            ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                                context.resources,
                                type,
                                labelResource
                            ).toString()
                        }
                    } else ""


                    phoneNumbersList.add(Pair(phoneNumber, label))
                }
            }
        }

        return phoneNumbersList
    }

    private fun getContactName(context: Context, contactId: String): String? {
        val nameCursor =
            context.contentResolver.query( // Use the content resolver of the provided Context to get information about contacts
                ContactsContract.Contacts.CONTENT_URI, // Look into the contacts data
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME), // Retrieve only the display name
                "${ContactsContract.Contacts._ID} = ?", // Look for contacts with a specific ID
                arrayOf(contactId), // Specify the contact ID
                null // No specific sorting order
            )

        // Safely use the nameCursor with the 'use' extension function
        nameCursor?.use { cursor ->
            // Find the index of the DISPLAY_NAME column in the cursor.
            val nameColumnIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

            // Check if there is data in the cursor and if the DISPLAY_NAME column index is valid
            if (cursor.moveToFirst() && nameColumnIndex != -1) {
                // If there is data and the column index is valid, get the name at the current cursor position
                return cursor.getString(nameColumnIndex)
            }
        }

        // If no name is found or an issue occurs, return null
        return null
    }

    private fun getContactImage(context: Context, contactId: String): Bitmap? {
        val contactUri = ContentUris.withAppendedId(
            ContactsContract.Contacts.CONTENT_URI,
            contactId.toLong()
        )

        val photoStream = ContactsContract.Contacts.openContactPhotoInputStream(
            context.contentResolver,
            contactUri
        )

        return BitmapFactory.decodeStream(photoStream)
    }


    private fun getEmailWithLabel(context: Context, contactId: String): List<Pair<String, String>> {
        val emailList = mutableListOf<Pair<String, String>>()
        val emailCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        emailCursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val emailColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                val labelColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL)
                val typeColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)

                if (emailColumnIndex != -1) {
                    val email = cursor.getString(emailColumnIndex)
                    val label = if (labelColumnIndex != -1) {
                        val type = cursor.getInt(typeColumnIndex)
                        val labelResource = cursor.getString(labelColumnIndex)
                        if (type == ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM)
                            labelResource
                        else {
                            ContactsContract.CommonDataKinds.Email.getTypeLabel(
                                context.resources,
                                type,
                                labelResource
                            ).toString()
                        }
                    } else ""

                    emailList.add(Pair(email, label))
                }
            }
        }

        return emailList
    }

    private fun getSignificantDateWithLabel(
        context: Context,
        contactId: String
    ): List<Pair<String, String>> {
        val significantDateList = mutableListOf<Pair<String, String>>()

        val dateCursor = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(contactId, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE),
            null
        )

        dateCursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val dateColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)
                val labelColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.LABEL)

                val typeColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE)

                if (dateColumnIndex != -1) {
                    val date = cursor.getString(dateColumnIndex)

                    val label = if (labelColumnIndex != -1) {
                        val type = cursor.getInt(typeColumnIndex)
                        val labelResource = cursor.getString(labelColumnIndex)

                        if (type == ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM)
                            labelResource
                        else {
                            ContactsContract.CommonDataKinds.Event.getTypeLabel(
                                context.resources,
                                type,
                                labelResource
                            ).toString()
                        }
                    } else ""
                    significantDateList.add(Pair(date, label))
                }
            }
        }

        return significantDateList
    }

    private fun getCompany(context: Context, contactId: String): String? {
        val organizationCursor = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(contactId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
            null
        )

        organizationCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val companyColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)

                if (companyColumnIndex != -1) {
                    return cursor.getString(companyColumnIndex)
                }
            }
        }

        return null
    }

    private fun getRelatedPersons(context: Context, contactId: String): List<Pair<String, String>> {
        val relatedPersonsList = mutableListOf<Pair<String, String>>()

        val relationCursor = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(contactId, ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE),
            null
        )

        relationCursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val nameColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Relation.NAME)

                val labelColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Relation.LABEL)

                val typeColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Relation.TYPE)

                if (nameColumnIndex != -1) {
                    val name = cursor.getString(nameColumnIndex)

                    val label = if (labelColumnIndex != -1) {
                        val type = cursor.getInt(typeColumnIndex)
                        val labelResource = cursor.getString(labelColumnIndex)

                        if (type == ContactsContract.CommonDataKinds.Relation.TYPE_CUSTOM)
                            labelResource
                        else {
                            ContactsContract.CommonDataKinds.Relation.getTypeLabel(
                                context.resources,
                                type,
                                labelResource
                            ).toString()
                        }
                    } else ""
                    relatedPersonsList.add(Pair(name, label))
                }
            }
        }

        return relatedPersonsList
    }

    private fun getNote(context: Context, contactId: String): String? {
        val noteCursor = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(contactId, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE),
            null
        )

        noteCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val noteColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)

                if (noteColumnIndex != -1) {
                    return cursor.getString(noteColumnIndex)
                }
            }
        }

        return null
    }

    companion object {
        private const val DEBOUNCE_TIMEOUT_MILLIS = 300L
    }
}
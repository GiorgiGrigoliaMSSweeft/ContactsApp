package com.example.contactsapp.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Item(
    val id: String = UUID.randomUUID().toString(),
    val bitmapImage: Bitmap? = null,
    val stringImage: String? = null,
    val name: String? = null,
    val phoneNumber: String? = null,

    val additionalPhoneNumbers: List<Pair<String, String>>? = null,
    val emailList: List<Pair<String, String>>? = null,
    val significantDate: List<Pair<String, String>>? = null,
    val company: String? = null,
    val relatedPersonsList: List<Pair<String, String>>? = null,
    val note: String? = null
) : Parcelable
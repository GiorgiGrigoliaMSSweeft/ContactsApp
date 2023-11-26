package com.example.contactsapp.model

import android.graphics.Bitmap
import java.util.UUID

//data class Item(
//    val id: String = UUID.randomUUID().toString(),
//    val bitmapImage: Bitmap? = null,
//    val stringImage: String? = null,
//    val name: String,
//    val phoneNumber: String
//)

data class Item(
    val id: String = UUID.randomUUID().toString(),
    val bitmapImage: Bitmap? = null,
    val stringImage: String? = null,
    val name: String? = null,
    val phoneNumber: String? = null,

    val secondaryPhoneNumber: List<Pair<String, String>>? = null,
    val emailList: List<Pair<String, String>>? = null,
    val significantDate: List<Pair<String, String>>? = null,
    val company: String? = null,
    val relatedPersonsList: List<Pair<String, String>>? = null,
    val note: String? = null

//    val secondaryNumber: String? = null,
//    val email: String? = null,
//    val significantDate: String? = null,
//    val company: String? = null,
//    val relatedPerson: String? = null,
//    val note: String? = null
)

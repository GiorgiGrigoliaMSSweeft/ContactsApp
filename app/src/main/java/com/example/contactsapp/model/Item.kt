package com.example.contactsapp.model

import android.graphics.Bitmap
import java.util.UUID

data class Item(
    val id: String = UUID.randomUUID().toString(),
    val bitmapImage: Bitmap? = null,
    val stringImage: String? = null,
    val name: String,
    val phoneNumber: String
)
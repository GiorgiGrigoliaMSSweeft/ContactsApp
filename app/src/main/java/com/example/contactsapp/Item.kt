package com.example.contactsapp

import android.graphics.Bitmap
import java.util.UUID

data class Item(
    val id: String = UUID.randomUUID().toString(),
    val image: Bitmap?,
    val name: String,
    val phoneNumber: String
)
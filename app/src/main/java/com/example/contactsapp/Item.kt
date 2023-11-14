package com.example.contactsapp

import android.graphics.Bitmap

data class Item(
    val image: Bitmap?,
    val name: String,
    val phoneNumber: String
)
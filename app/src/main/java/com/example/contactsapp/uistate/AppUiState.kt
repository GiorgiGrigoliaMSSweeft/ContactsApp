package com.example.contactsapp.uistate

import com.example.contactsapp.model.Item

data class AppUiState(
    val userInput: String = "",
    val retrievedContacts: List<Item> = emptyList()
)
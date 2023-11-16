package com.example.contactsapp

data class AppUiState(
    val userInput: String = "",
    val retrievedContacts: List<Item> = emptyList()
)
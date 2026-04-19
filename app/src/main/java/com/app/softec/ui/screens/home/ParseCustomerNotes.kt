package com.app.softec.ui.screens.home

fun parseCustomerNotes(notes: String?): Pair<String, String> {
    if (notes.isNullOrBlank()) {
        return "" to ""
    }
    val segments = notes.split(" • ").map { it.trim() }
    val phone = segments.firstOrNull { it.startsWith("Phone: ") }
        ?.removePrefix("Phone: ")
        ?.trim()
        .orEmpty()
    val email = segments.firstOrNull { it.startsWith("Email: ") }
        ?.removePrefix("Email: ")
        ?.trim()
        .orEmpty()
    return phone to email
}
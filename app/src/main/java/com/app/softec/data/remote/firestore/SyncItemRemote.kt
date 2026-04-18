package com.app.softec.data.remote.firestore

import java.util.Date

data class SyncItemRemote(
    val id: String = "",
    val title: String = "",
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    val updatedAt: Date = Date()
)

package com.app.softec.data.remote.firestore

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

inline fun <reified T : Any> CollectionReference.snapshotAsFlow(): Flow<List<T>> {
    return callbackFlow {
        val registration = addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val result = snapshot?.documents
                ?.mapNotNull { it.toObject(T::class.java) }
                .orEmpty()
            trySend(result)
        }
        awaitClose { registration.remove() }
    }
}

inline fun <reified T : Any> Query.snapshotAsFlow(): Flow<List<T>> {
    return callbackFlow {
        val registration = addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val result = snapshot?.documents
                ?.mapNotNull { it.toObject(T::class.java) }
                .orEmpty()
            trySend(result)
        }
        awaitClose { registration.remove() }
    }
}

suspend fun DocumentReference.save(data: Any, merge: Boolean = true) {
    if (merge) {
        set(data, SetOptions.merge()).await()
    } else {
        set(data).await()
    }
}

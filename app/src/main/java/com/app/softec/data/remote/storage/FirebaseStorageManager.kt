package com.app.softec.data.remote.storage

import android.net.Uri
import com.app.softec.core.result.Resource
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageManager @Inject constructor(
    private val storage: FirebaseStorage
) : StorageManager {

    override suspend fun upload(uri: Uri, remotePath: String): Resource<String> {
        return try {
            val reference = storage.reference.child(remotePath)
            reference.putFile(uri).await()
            val downloadUrl = reference.downloadUrl.await().toString()
            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Upload failed", throwable = e)
        }
    }
}

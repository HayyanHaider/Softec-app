package com.app.softec.data.remote.storage

import android.net.Uri
import com.app.softec.core.result.Resource

interface StorageManager {
    suspend fun upload(uri: Uri, remotePath: String): Resource<String>
}

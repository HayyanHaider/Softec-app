package com.app.softec.core.result

sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : Resource<Nothing>()
}

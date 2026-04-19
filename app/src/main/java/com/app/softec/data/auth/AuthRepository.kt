package com.app.softec.data.auth

import com.app.softec.core.result.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun authState(): Flow<FirebaseUser?>
    suspend fun signInWithEmail(email: String, password: String): Resource<FirebaseUser>
    suspend fun signUpWithEmail(email: String, password: String): Resource<FirebaseUser>
    suspend fun signOut(): Resource<Unit>
}

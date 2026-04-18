package com.app.softec.data.auth

import com.app.softec.core.result.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun authState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val user = firebaseAuth.signInWithEmailAndPassword(email, password).await().user
            if (user == null) {
                Resource.Error("Email sign-in failed: no user returned")
            } else {
                Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Email sign-in failed", throwable = e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val user = firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
            if (user == null) {
                Resource.Error("Sign-up failed: no user returned")
            } else {
                Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Sign-up failed", throwable = e)
        }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Resource<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val user = firebaseAuth.signInWithCredential(credential).await().user
            if (user == null) {
                Resource.Error("Google sign-in failed: no user returned")
            } else {
                Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Google sign-in failed", throwable = e)
        }
    }

    override suspend fun signOut(): Resource<Unit> {
        return try {
            firebaseAuth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Sign out failed", throwable = e)
        }
    }
}

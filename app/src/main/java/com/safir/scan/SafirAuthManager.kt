package com.safir.scan

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Authentication core for Safir Scanner.
 *
 * Provider UI must remain hidden unless AuthPublicConfig.isReady is true and the exact
 * Firebase/Google/Apple provider setup has passed the reviewer gate. The scanner itself
 * remains usable without an account.
 */
class SafirAuthManager private constructor(
    private val context: Context,
    private val config: AuthPublicConfig,
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager
) {
    data class Session(
        val uid: String,
        val email: String?,
        val displayName: String?,
        val emailVerified: Boolean,
        val providers: List<String>
    )

    sealed interface Outcome {
        data class SignedIn(val session: Session) : Outcome
        data class VerificationSent(val email: String) : Outcome
        data object PasswordResetSent : Outcome
        data object SignedOut : Outcome
        data object AccountDeleted : Outcome
        data class ReauthenticationRequired(val message: String) : Outcome
        data class ProviderLinkConsentRequired(val message: String) : Outcome
        data class Failure(val message: String) : Outcome
    }

    fun currentSession(): Session? = auth.currentUser?.toSession()

    suspend fun createEmailAccount(email: String, password: String): Outcome {
        if (email.isBlank() || password.isBlank()) return Outcome.Failure("Enter a valid email and password.")
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).awaitValue()
            val user = result.user ?: return Outcome.Failure("Account could not be created.")
            user.sendEmailVerification().awaitCompletion()
            auth.signOut()
            Outcome.VerificationSent(email.trim())
        } catch (error: Throwable) {
            mapFailure(error)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Outcome {
        if (email.isBlank() || password.isBlank()) return Outcome.Failure("Enter a valid email and password.")
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).awaitValue()
            val user = result.user ?: return Outcome.Failure("Sign-in could not be completed.")
            user.reload().awaitCompletion()
            val refreshed = auth.currentUser ?: user
            if (!refreshed.isEmailVerified) {
                runCatching { refreshed.sendEmailVerification().awaitCompletion() }
                auth.signOut()
                Outcome.VerificationSent(email.trim())
            } else {
                Outcome.SignedIn(refreshed.toSession())
            }
        } catch (error: Throwable) {
            mapFailure(error)
        }
    }

    suspend fun sendPasswordReset(email: String): Outcome {
        if (email.isBlank()) return Outcome.Failure("Enter your email address.")
        return try {
            auth.sendPasswordResetEmail(email.trim()).awaitCompletion()
            Outcome.PasswordResetSent
        } catch (error: Throwable) {
            // Keep wording intentionally generic so the UI does not reveal account existence.
            when (error) {
                is FirebaseNetworkException -> Outcome.Failure("Network unavailable. Try again when you are online.")
                else -> Outcome.PasswordResetSent
            }
        }
    }

    suspend fun signInWithGoogle(activity: Activity): Outcome {
        return try {
            val credential = getGoogleCredential(activity)
            val result = auth.signInWithCredential(credential).awaitValue()
            val user = result.user ?: return Outcome.Failure("Google sign-in could not be completed.")
            Outcome.SignedIn(user.toSession())
        } catch (error: Throwable) {
            mapFailure(error)
        }
    }

    suspend fun signInWithApple(activity: Activity): Outcome {
        return try {
            val provider = appleProvider()
            val pending = auth.pendingAuthResult
            val result = if (pending != null) {
                pending.awaitValue()
            } else {
                auth.startActivityForSignInWithProvider(activity, provider.build()).awaitValue()
            }
            val user = result.user ?: return Outcome.Failure("Apple sign-in could not be completed.")
            Outcome.SignedIn(user.toSession())
        } catch (error: Throwable) {
            mapFailure(error)
        }
    }

    suspend fun linkGoogle(activity: Activity): Outcome {
        val user = auth.currentUser ?: return Outcome.Failure("Sign in before linking another provider.")
        return try {
            val credential = getGoogleCredential(activity)
            val result = user.linkWithCredential(credential).awaitValue()
            val linked = result.user ?: user
            Outcome.SignedIn(linked.toSession())
        } catch (error: Throwable) {
            mapFailure(error)
        }
    }

    suspend fun linkApple(activity: Activity, explicitConsent: Boolean): Outcome {
        if (!explicitConsent) {
            return Outcome.ProviderLinkConsentRequired(
                "Apple requires explicit consent before linking an Apple identity to other identifying account data."
            )
        }
        val user = auth.currentUser ?: return Outcome.Failure("Sign in before linking another provider.")
        return try {
            val result = user.startActivityForLinkWithProvider(activity, appleProvider().build()).awaitValue()
            val linked = result.user ?: user
            Outcome.SignedIn(linked.toSession())
        } catch (error: Throwable) {
            mapFailure(error)
        }
    }

    suspend fun reauthenticateEmail(email: String, password: String): Outcome {
        val user = auth.currentUser ?: return Outcome.Failure("No signed-in account.")
        return try {
            user.reauthenticate(EmailAuthProvider.getCredential(email.trim(), password)).awaitCompletion()
            Outcome.SignedIn(user.toSession())
        } catch (error: Throwable) {
            mapFailure(error)
        }
    }

    suspend fun reauthenticateGoogle(activity: Activity): Outcome {
        val user = auth.currentUser ?: return Outcome.Failure("No signed-in account.")
        return try {
            user.reauthenticate(getGoogleCredential(activity)).awaitCompletion()
            Outcome.SignedIn(user.toSession())
        } catch (error: Throwable) {
            mapFailure(error)
        }
    }

    suspend fun reauthenticateApple(activity: Activity): Outcome {
        val user = auth.currentUser ?: return Outcome.Failure("No signed-in account.")
        return try {
            user.startActivityForReauthenticateWithProvider(activity, appleProvider().build()).awaitValue()
            Outcome.SignedIn(user.toSession())
        } catch (error: Throwable) {
            mapFailure(error)
        }
    }

    suspend fun signOut(): Outcome {
        auth.signOut()
        runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
        return Outcome.SignedOut
    }

    suspend fun deleteAccount(): Outcome {
        val user = auth.currentUser ?: return Outcome.Failure("No signed-in account.")
        return try {
            user.delete().awaitCompletion()
            runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
            Outcome.AccountDeleted
        } catch (error: FirebaseAuthRecentLoginRequiredException) {
            Outcome.ReauthenticationRequired("Sign in again before deleting the account.")
        } catch (error: Throwable) {
            mapFailure(error)
        }
    }

    private suspend fun getGoogleCredential(activity: Activity): AuthCredential {
        suspend fun request(authorizedOnly: Boolean): AuthCredential {
            val option = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(authorizedOnly)
                .setServerClientId(config.googleWebClientId)
                .setAutoSelectEnabled(false)
                .build()
            val response = credentialManager.getCredential(
                context = activity,
                request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            )
            val credential = response.credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                throw IllegalStateException("Unexpected Google credential type")
            }
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            return GoogleAuthProvider.getCredential(googleId.idToken, null)
        }

        return try {
            request(authorizedOnly = true)
        } catch (_: NoCredentialException) {
            request(authorizedOnly = false)
        }
    }

    private fun appleProvider(): OAuthProvider.Builder =
        OAuthProvider.newBuilder("apple.com").apply {
            setScopes(arrayOf("email", "name"))
        }

    private fun FirebaseUser.toSession() = Session(
        uid = uid,
        email = email,
        displayName = displayName,
        emailVerified = isEmailVerified,
        providers = providerData.mapNotNull { it.providerId.takeIf(String::isNotBlank) }.distinct()
    )

    private fun mapFailure(error: Throwable): Outcome = when (error) {
        is FirebaseNetworkException -> Outcome.Failure("Network unavailable. Try again when you are online.")
        is FirebaseAuthWeakPasswordException -> Outcome.Failure("Choose a stronger password.")
        is FirebaseAuthUserCollisionException -> Outcome.Failure("This identity is already linked to another account.")
        is FirebaseAuthRecentLoginRequiredException -> Outcome.ReauthenticationRequired("Sign in again to continue.")
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> Outcome.Failure("Sign-in could not be completed. Check your details and try again.")
        else -> Outcome.Failure("Authentication could not be completed. Try again.")
    }

    companion object {
        private const val FIREBASE_APP_NAME = "safir-scanner-auth"

        fun create(context: Context, config: AuthPublicConfig = AuthPublicConfig.fromBuildConfig()): SafirAuthManager? {
            if (!config.isReady) return null
            val appContext = context.applicationContext
            val firebaseApp = FirebaseApp.getApps(appContext).firstOrNull { it.name == FIREBASE_APP_NAME }
                ?: FirebaseApp.initializeApp(
                    appContext,
                    FirebaseOptions.Builder()
                        .setApplicationId(config.firebaseAppId)
                        .setApiKey(config.firebaseApiKey)
                        .setProjectId(config.firebaseProjectId)
                        .apply {
                            if (config.firebaseSenderId.isNotBlank()) setGcmSenderId(config.firebaseSenderId)
                        }
                        .build(),
                    FIREBASE_APP_NAME
                )
            return SafirAuthManager(
                context = appContext,
                config = config,
                auth = FirebaseAuth.getInstance(firebaseApp),
                credentialManager = CredentialManager.create(appContext)
            )
        }
    }
}

private suspend fun Task<*>.awaitCompletion() {
    suspendCoroutine<Unit> { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) continuation.resume(Unit)
            else continuation.resumeWithException(task.exception ?: IllegalStateException("Authentication task failed"))
        }
    }
}

private suspend fun <T> Task<T>.awaitValue(): T =
    suspendCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val value = task.result
                if (value != null) continuation.resume(value)
                else continuation.resumeWithException(IllegalStateException("Authentication task returned no result"))
            } else {
                continuation.resumeWithException(task.exception ?: IllegalStateException("Authentication task failed"))
            }
        }
    }

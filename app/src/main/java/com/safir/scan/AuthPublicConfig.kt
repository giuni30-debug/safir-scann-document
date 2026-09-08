package com.safir.scan

data class AuthPublicConfig(
    val enabled: Boolean,
    val firebaseApiKey: String,
    val firebaseAppId: String,
    val firebaseProjectId: String,
    val firebaseSenderId: String,
    val googleWebClientId: String
) {
    val missingRequiredFields: List<String>
        get() = buildList {
            if (firebaseApiKey.isBlank()) add("SAFIR_FIREBASE_API_KEY")
            if (firebaseAppId.isBlank()) add("SAFIR_FIREBASE_APP_ID")
            if (firebaseProjectId.isBlank()) add("SAFIR_FIREBASE_PROJECT_ID")
            if (googleWebClientId.isBlank()) add("SAFIR_GOOGLE_WEB_CLIENT_ID")
        }

    val isReady: Boolean
        get() = enabled && missingRequiredFields.isEmpty()

    companion object {
        fun fromBuildConfig() = AuthPublicConfig(
            enabled = BuildConfig.AUTH_ENABLED,
            firebaseApiKey = BuildConfig.FIREBASE_API_KEY,
            firebaseAppId = BuildConfig.FIREBASE_APP_ID,
            firebaseProjectId = BuildConfig.FIREBASE_PROJECT_ID,
            firebaseSenderId = BuildConfig.FIREBASE_SENDER_ID,
            googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        )
    }
}

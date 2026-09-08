package com.safir.scan

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthPublicConfigTest {
    @Test
    fun disabledConfig_isNeverReady() {
        val config = AuthPublicConfig(
            enabled = false,
            firebaseApiKey = "api",
            firebaseAppId = "app",
            firebaseProjectId = "project",
            firebaseSenderId = "sender",
            googleWebClientId = "web"
        )
        assertFalse(config.isReady)
    }

    @Test
    fun enabledButIncompleteConfig_isNotReady() {
        val config = AuthPublicConfig(
            enabled = true,
            firebaseApiKey = "api",
            firebaseAppId = "",
            firebaseProjectId = "project",
            firebaseSenderId = "",
            googleWebClientId = "web"
        )
        assertFalse(config.isReady)
        assertTrue("SAFIR_FIREBASE_APP_ID" in config.missingRequiredFields)
    }

    @Test
    fun enabledCompleteConfig_isReady() {
        val config = AuthPublicConfig(
            enabled = true,
            firebaseApiKey = "api",
            firebaseAppId = "app",
            firebaseProjectId = "project",
            firebaseSenderId = "",
            googleWebClientId = "web"
        )
        assertTrue(config.isReady)
        assertTrue(config.missingRequiredFields.isEmpty())
    }
}

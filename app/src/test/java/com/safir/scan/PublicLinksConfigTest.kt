package com.safir.scan

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PublicLinksConfigTest {
    @Test
    fun requiresHttpsPublicReleaseLinks() {
        val ready = PublicLinksConfig(
            enabled = true,
            privacyUrl = "https://example.org/privacy",
            supportUrl = "https://example.org/support",
            termsUrl = "https://example.org/terms",
            developerWebsite = "https://example.org",
            deleteAccountUrl = "https://example.org/delete-account"
        )
        assertTrue(ready.isReady)
        assertTrue(ready.termsReady)
        assertTrue(ready.deleteAccountReady)
    }

    @Test
    fun rejectsHttpOrPlaceholderLikeEmptyLinks() {
        val bad = PublicLinksConfig(
            enabled = true,
            privacyUrl = "http://example.org/privacy",
            supportUrl = "",
            termsUrl = "",
            developerWebsite = "https://example.org",
            deleteAccountUrl = ""
        )
        assertFalse(bad.isReady)
        assertFalse(bad.privacyReady)
        assertFalse(bad.supportReady)
        assertFalse(bad.termsReady)
        assertFalse(bad.deleteAccountReady)
    }
}

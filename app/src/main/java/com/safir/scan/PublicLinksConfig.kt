package com.safir.scan

import java.net.URI

data class PublicLinksConfig(
    val enabled: Boolean,
    val privacyUrl: String,
    val supportUrl: String,
    val termsUrl: String,
    val developerWebsite: String,
    val deleteAccountUrl: String
) {
    val privacyReady: Boolean get() = isHttpsUrl(privacyUrl)
    val supportReady: Boolean get() = isHttpsUrl(supportUrl)
    val developerWebsiteReady: Boolean get() = isHttpsUrl(developerWebsite)
    val termsReady: Boolean get() = termsUrl.isNotBlank() && isHttpsUrl(termsUrl)
    val deleteAccountReady: Boolean get() = deleteAccountUrl.isNotBlank() && isHttpsUrl(deleteAccountUrl)

    val isReady: Boolean
        get() = enabled && privacyReady && supportReady && developerWebsiteReady

    companion object {
        fun fromBuildConfig() = PublicLinksConfig(
            enabled = BuildConfig.PUBLIC_LINKS_ENABLED,
            privacyUrl = BuildConfig.PRIVACY_URL,
            supportUrl = BuildConfig.SUPPORT_URL,
            termsUrl = BuildConfig.TERMS_URL,
            developerWebsite = BuildConfig.DEVELOPER_WEBSITE_URL,
            deleteAccountUrl = BuildConfig.DELETE_ACCOUNT_URL
        )

        fun isHttpsUrl(value: String): Boolean {
            if (value.isBlank()) return false
            return runCatching {
                val uri = URI(value.trim())
                uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
            }.getOrDefault(false)
        }
    }
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val authEnabledValue = providers.environmentVariable("SAFIR_AUTH_ENABLED")
    .orElse(providers.gradleProperty("SAFIR_AUTH_ENABLED"))
    .orElse("false")
    .get()
val firebaseApiKeyValue = providers.environmentVariable("SAFIR_FIREBASE_API_KEY")
    .orElse(providers.gradleProperty("SAFIR_FIREBASE_API_KEY"))
    .orElse("")
    .get()
val firebaseAppIdValue = providers.environmentVariable("SAFIR_FIREBASE_APP_ID")
    .orElse(providers.gradleProperty("SAFIR_FIREBASE_APP_ID"))
    .orElse("")
    .get()
val firebaseProjectIdValue = providers.environmentVariable("SAFIR_FIREBASE_PROJECT_ID")
    .orElse(providers.gradleProperty("SAFIR_FIREBASE_PROJECT_ID"))
    .orElse("")
    .get()
val firebaseSenderIdValue = providers.environmentVariable("SAFIR_FIREBASE_SENDER_ID")
    .orElse(providers.gradleProperty("SAFIR_FIREBASE_SENDER_ID"))
    .orElse("")
    .get()
val googleWebClientIdValue = providers.environmentVariable("SAFIR_GOOGLE_WEB_CLIENT_ID")
    .orElse(providers.gradleProperty("SAFIR_GOOGLE_WEB_CLIENT_ID"))
    .orElse("")
    .get()

val publicLinksEnabledValue = providers.environmentVariable("SAFIR_PUBLIC_LINKS_ENABLED")
    .orElse(providers.gradleProperty("SAFIR_PUBLIC_LINKS_ENABLED"))
    .orElse("false")
    .get()
val privacyUrlValue = providers.environmentVariable("SAFIR_PRIVACY_URL")
    .orElse(providers.gradleProperty("SAFIR_PRIVACY_URL"))
    .orElse("")
    .get()
val supportUrlValue = providers.environmentVariable("SAFIR_SUPPORT_URL")
    .orElse(providers.gradleProperty("SAFIR_SUPPORT_URL"))
    .orElse("")
    .get()
val termsUrlValue = providers.environmentVariable("SAFIR_TERMS_URL")
    .orElse(providers.gradleProperty("SAFIR_TERMS_URL"))
    .orElse("")
    .get()
val developerWebsiteUrlValue = providers.environmentVariable("SAFIR_DEVELOPER_WEBSITE_URL")
    .orElse(providers.gradleProperty("SAFIR_DEVELOPER_WEBSITE_URL"))
    .orElse("")
    .get()
val deleteAccountUrlValue = providers.environmentVariable("SAFIR_DELETE_ACCOUNT_URL")
    .orElse(providers.gradleProperty("SAFIR_DELETE_ACCOUNT_URL"))
    .orElse("")
    .get()

val authEnabled = authEnabledValue.equals("true", ignoreCase = true)
if (authEnabled) {
    val missingAuthConfig = listOf(
        "SAFIR_FIREBASE_API_KEY" to firebaseApiKeyValue,
        "SAFIR_FIREBASE_APP_ID" to firebaseAppIdValue,
        "SAFIR_FIREBASE_PROJECT_ID" to firebaseProjectIdValue,
        "SAFIR_GOOGLE_WEB_CLIENT_ID" to googleWebClientIdValue
    ).filter { it.second.isBlank() }.map { it.first }
    check(missingAuthConfig.isEmpty()) {
        "SAFIR_AUTH_ENABLED=true but required auth configuration is missing: ${missingAuthConfig.joinToString()}"
    }
}

fun isHttpsUrl(value: String): Boolean =
    value.trim().startsWith("https://", ignoreCase = true) && value.substringAfter("https://", "").isNotBlank()

val publicLinksEnabled = publicLinksEnabledValue.equals("true", ignoreCase = true)
if (publicLinksEnabled) {
    val requiredLinks = listOf(
        "SAFIR_PRIVACY_URL" to privacyUrlValue,
        "SAFIR_SUPPORT_URL" to supportUrlValue,
        "SAFIR_DEVELOPER_WEBSITE_URL" to developerWebsiteUrlValue
    )
    val missingLinks = requiredLinks.filter { it.second.isBlank() }.map { it.first }
    check(missingLinks.isEmpty()) {
        "SAFIR_PUBLIC_LINKS_ENABLED=true but required public URLs are missing: ${missingLinks.joinToString()}"
    }
    val insecureLinks = requiredLinks.filterNot { isHttpsUrl(it.second) }.map { it.first }
    check(insecureLinks.isEmpty()) {
        "Public release URLs must use HTTPS: ${insecureLinks.joinToString()}"
    }
    if (termsUrlValue.isNotBlank()) {
        check(isHttpsUrl(termsUrlValue)) { "SAFIR_TERMS_URL must use HTTPS when configured" }
    }
    if (authEnabled) {
        check(deleteAccountUrlValue.isNotBlank() && isHttpsUrl(deleteAccountUrlValue)) {
            "Auth-enabled release requires an HTTPS SAFIR_DELETE_ACCOUNT_URL"
        }
    } else if (deleteAccountUrlValue.isNotBlank()) {
        check(isHttpsUrl(deleteAccountUrlValue)) { "SAFIR_DELETE_ACCOUNT_URL must use HTTPS when configured" }
    }
}

fun buildConfigString(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.safir.scan"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.safir.scan"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0"

        buildConfigField("boolean", "AUTH_ENABLED", authEnabled.toString())
        buildConfigField("String", "FIREBASE_API_KEY", buildConfigString(firebaseApiKeyValue))
        buildConfigField("String", "FIREBASE_APP_ID", buildConfigString(firebaseAppIdValue))
        buildConfigField("String", "FIREBASE_PROJECT_ID", buildConfigString(firebaseProjectIdValue))
        buildConfigField("String", "FIREBASE_SENDER_ID", buildConfigString(firebaseSenderIdValue))
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", buildConfigString(googleWebClientIdValue))

        buildConfigField("boolean", "PUBLIC_LINKS_ENABLED", publicLinksEnabled.toString())
        buildConfigField("String", "PRIVACY_URL", buildConfigString(privacyUrlValue))
        buildConfigField("String", "SUPPORT_URL", buildConfigString(supportUrlValue))
        buildConfigField("String", "TERMS_URL", buildConfigString(termsUrlValue))
        buildConfigField("String", "DEVELOPER_WEBSITE_URL", buildConfigString(developerWebsiteUrlValue))
        buildConfigField("String", "DELETE_ACCOUNT_URL", buildConfigString(deleteAccountUrlValue))
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    implementation("org.opencv:opencv:4.12.0")

    // Bundled on-device Latin-script OCR. No model download is required at first use.
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // Authentication foundation. Provider buttons stay hidden until exact production
    // Firebase/Google/Apple configuration passes docs/auth-login-gate.md.
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    testImplementation("junit:junit:4.13.2")
}

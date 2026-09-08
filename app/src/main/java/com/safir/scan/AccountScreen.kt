package com.safir.scan

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val AccountWhite = Color(0xFFF9FBFF)
private val AccountIce = Color(0xFFDDF8FF)
private val AccountMint = Color(0xFF79FFD2)
private val AccountGlass = Color(0x2EFFFFFF)
private val AccountBorder = Color(0x55FFFFFF)
private val AccountDeep = Color(0xFF301274)

/**
 * This screen is reachable only when AuthPublicConfig.isReady and a real auth manager exist.
 * Core scanning remains available without an account.
 */
@Composable
fun AccountScreen(
    manager: SafirAuthManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var session by remember { mutableStateOf(manager.currentSession()) }
    var email by remember { mutableStateOf(session?.email.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var deleteArmed by remember { mutableStateOf(false) }
    var appleLinkConsentArmed by remember { mutableStateOf(false) }

    fun launchAction(action: suspend () -> SafirAuthManager.Outcome) {
        if (busy) return
        busy = true
        message = null
        scope.launch {
            val outcome = runCatching { action() }
                .getOrElse { SafirAuthManager.Outcome.Failure("Authentication could not be completed. Try again.") }
            busy = false
            when (outcome) {
                is SafirAuthManager.Outcome.SignedIn -> {
                    session = outcome.session
                    email = outcome.session.email.orEmpty()
                    password = ""
                    message = "Account ready."
                }
                is SafirAuthManager.Outcome.VerificationSent -> {
                    session = manager.currentSession()
                    password = ""
                    message = "Verification email sent. Verify it before signing in."
                }
                SafirAuthManager.Outcome.PasswordResetSent ->
                    message = "If the address is eligible, a password recovery email has been sent."
                SafirAuthManager.Outcome.SignedOut -> {
                    session = null
                    password = ""
                    deleteArmed = false
                    appleLinkConsentArmed = false
                    message = "Signed out."
                }
                SafirAuthManager.Outcome.AccountDeleted -> {
                    session = null
                    password = ""
                    deleteArmed = false
                    appleLinkConsentArmed = false
                    message = "Account deleted. Local PDFs remain on this device."
                }
                is SafirAuthManager.Outcome.ReauthenticationRequired ->
                    message = outcome.message
                is SafirAuthManager.Outcome.ProviderLinkConsentRequired -> {
                    appleLinkConsentArmed = true
                    message = outcome.message
                }
                is SafirAuthManager.Outcome.Failure -> message = outcome.message
            }
        }
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(Color(0xFF315BCB), Color(0xFF4936AE), Color(0xFF742EAF), Color(0xFF3154C4))
            )
        ).statusBarsPadding().navigationBarsPadding().padding(18.dp)
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    enabled = !busy,
                    onClick = onBack,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccountGlass)
                ) { Text("← Back", color = AccountWhite) }
                Text(
                    "ACCOUNT",
                    color = AccountWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            AccountCard("Optional account") {
                Text(
                    "Scanning and local PDF storage work without signing in.",
                    color = AccountMint,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Authentication never grants access to Gmail, Drive or Calendar.",
                    color = AccountIce,
                    fontSize = 12.sp
                )
            }

            val current = session
            if (current == null) {
                AccountCard("Sign in") {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        enabled = !busy,
                        singleLine = true,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        enabled = !busy,
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        enabled = !busy,
                        onClick = { launchAction { manager.signInWithEmail(email, password) } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccountMint)
                    ) { Text("Sign in with email", color = AccountDeep, fontWeight = FontWeight.Black) }
                    Spacer(Modifier.height(7.dp))
                    Button(
                        enabled = !busy,
                        onClick = { launchAction { manager.createEmailAccount(email, password) } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccountGlass)
                    ) { Text("Create email account", color = AccountWhite) }
                    Spacer(Modifier.height(7.dp))
                    Button(
                        enabled = !busy && email.isNotBlank(),
                        onClick = { launchAction { manager.sendPasswordReset(email) } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccountGlass)
                    ) { Text("Reset password", color = AccountWhite) }
                }

                if (activity != null) {
                    AccountCard("Google & Apple") {
                        Button(
                            enabled = !busy,
                            onClick = { launchAction { manager.signInWithGoogle(activity) } },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccountWhite)
                        ) { Text("Continue with Google", color = AccountDeep, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            enabled = !busy,
                            onClick = { launchAction { manager.signInWithApple(activity) } },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccountWhite)
                        ) { Text("Continue with Apple", color = AccountDeep, fontWeight = FontWeight.Bold) }
                    }
                }
            } else {
                AccountCard("Signed in") {
                    Text(current.displayName ?: current.email ?: "Safir account", color = AccountWhite, fontWeight = FontWeight.Black)
                    current.email?.let { Text(it, color = AccountIce, fontSize = 12.sp) }
                    Text(
                        "Providers: ${current.providers.filter { it != "firebase" }.joinToString().ifBlank { "account" }}",
                        color = AccountIce,
                        fontSize = 11.sp
                    )
                }

                if (activity != null) {
                    AccountCard("Connected sign-in methods") {
                        Button(
                            enabled = !busy && "google.com" !in current.providers,
                            onClick = { launchAction { manager.linkGoogle(activity) } },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccountGlass)
                        ) { Text(if ("google.com" in current.providers) "Google connected" else "Connect Google", color = AccountWhite) }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            enabled = !busy && "apple.com" !in current.providers,
                            onClick = {
                                if (appleLinkConsentArmed) {
                                    launchAction { manager.linkApple(activity, explicitConsent = true) }
                                    appleLinkConsentArmed = false
                                } else {
                                    appleLinkConsentArmed = true
                                    message = "Tap Connect Apple again to confirm linking this Apple identity to your Safir account."
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccountGlass)
                        ) {
                            Text(
                                when {
                                    "apple.com" in current.providers -> "Apple connected"
                                    appleLinkConsentArmed -> "Confirm Connect Apple"
                                    else -> "Connect Apple"
                                },
                                color = AccountWhite
                            )
                        }
                    }
                }

                AccountCard("Account controls") {
                    Button(
                        enabled = !busy,
                        onClick = { launchAction { manager.signOut() } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccountGlass)
                    ) { Text("Sign out", color = AccountWhite) }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        enabled = !busy,
                        onClick = {
                            if (deleteArmed) {
                                launchAction { manager.deleteAccount() }
                            } else {
                                deleteArmed = true
                                message = "Account deletion is permanent. Tap Delete account again to confirm."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x55FF6F9C))
                    ) { Text(if (deleteArmed) "Confirm delete account" else "Delete account", color = AccountWhite) }
                    if (deleteArmed && current.email != null && "password" in current.providers) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "If deletion asks you to sign in again, enter your password above and reauthenticate before retrying.",
                            color = AccountIce,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            message?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth().border(1.dp, AccountBorder, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    color = AccountGlass
                ) {
                    Text(it, color = AccountIce, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(14.dp))
                }
            }

            if (busy) {
                Text("Working…", color = AccountMint, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AccountCard(title: String, content: @Composable Column.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, AccountBorder, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = AccountGlass
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = AccountWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

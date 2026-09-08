package com.safir.scan

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val AuthWhite = Color(0xFFF9FBFF)
private val AuthIce = Color(0xFFDDF8FF)
private val AuthMint = Color(0xFF79FFD2)
private val AuthGlass = Color(0x2EFFFFFF)
private val AuthBorder = Color(0x55FFFFFF)
private val AuthDeep = Color(0xFF301274)

/** Reachable only when AuthPublicConfig.isReady. Core scanning never requires an account. */
@Composable
fun AccountScreen(manager: SafirAuthManager, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    var session by remember { mutableStateOf(manager.currentSession()) }
    var email by remember { mutableStateOf(session?.email.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var deleteArmed by remember { mutableStateOf(false) }
    var appleLinkArmed by remember { mutableStateOf(false) }

    fun runAuth(block: suspend () -> SafirAuthManager.Outcome) {
        if (busy) return
        busy = true
        message = null
        scope.launch {
            val result = runCatching { block() }
                .getOrElse { SafirAuthManager.Outcome.Failure("Authentication could not be completed. Try again.") }
            busy = false
            when (result) {
                is SafirAuthManager.Outcome.SignedIn -> {
                    session = result.session
                    email = result.session.email.orEmpty()
                    password = ""
                    deleteArmed = false
                    appleLinkArmed = false
                    message = "Account ready."
                }
                is SafirAuthManager.Outcome.VerificationSent -> {
                    session = manager.currentSession()
                    password = ""
                    message = "Verification email sent. Verify it before signing in."
                }
                SafirAuthManager.Outcome.PasswordResetSent ->
                    message = "If the address is eligible, a recovery email has been sent."
                SafirAuthManager.Outcome.SignedOut -> {
                    session = null
                    password = ""
                    deleteArmed = false
                    message = "Signed out."
                }
                SafirAuthManager.Outcome.AccountDeleted -> {
                    session = null
                    password = ""
                    deleteArmed = false
                    message = "Account deleted. Local PDFs remain on this device."
                }
                is SafirAuthManager.Outcome.ReauthenticationRequired -> message = result.message
                is SafirAuthManager.Outcome.ProviderLinkConsentRequired -> message = result.message
                is SafirAuthManager.Outcome.Failure -> message = result.message
            }
        }
    }

    Column(
        Modifier.fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFF315BCB), Color(0xFF4936AE), Color(0xFF742EAF), Color(0xFF3154C4))))
            .statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(enabled = !busy, onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = AuthGlass)) {
                Text("← Back", color = AuthWhite)
            }
            Text("ACCOUNT", color = AuthWhite, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 9.dp))
        }

        AuthCard("Optional account") {
            Text("Scanning and local PDFs work without signing in.", color = AuthMint, fontWeight = FontWeight.Bold)
            Text("Identity sign-in does not request Gmail, Drive or Calendar access.", color = AuthIce, fontSize = 12.sp)
        }

        val current = session
        if (current == null) {
            AuthCard("Email") {
                OutlinedTextField(email, { email = it }, enabled = !busy, singleLine = true, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    password, { password = it }, enabled = !busy, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), label = { Text("Password") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                AuthButton("Sign in with email", primary = true, enabled = !busy) { runAuth { manager.signInWithEmail(email, password) } }
                AuthButton("Create email account", enabled = !busy) { runAuth { manager.createEmailAccount(email, password) } }
                AuthButton("Reset password", enabled = !busy && email.isNotBlank()) { runAuth { manager.sendPasswordReset(email) } }
            }
            if (activity != null) {
                AuthCard("Google & Apple") {
                    AuthButton("Continue with Google", primary = true, enabled = !busy) { runAuth { manager.signInWithGoogle(activity) } }
                    AuthButton("Continue with Apple", primary = true, enabled = !busy) { runAuth { manager.signInWithApple(activity) } }
                }
            }
        } else {
            AuthCard("Signed in") {
                Text(current.displayName ?: current.email ?: "Safir account", color = AuthWhite, fontWeight = FontWeight.Black)
                current.email?.let { Text(it, color = AuthIce, fontSize = 12.sp) }
                Text("Providers: ${current.providers.filter { it != "firebase" }.joinToString().ifBlank { "account" }}", color = AuthIce, fontSize = 11.sp)
            }
            if (activity != null) {
                AuthCard("Connected sign-in methods") {
                    AuthButton(
                        if ("google.com" in current.providers) "Google connected" else "Connect Google",
                        enabled = !busy && "google.com" !in current.providers
                    ) { runAuth { manager.linkGoogle(activity) } }
                    AuthButton(
                        when {
                            "apple.com" in current.providers -> "Apple connected"
                            appleLinkArmed -> "Confirm Connect Apple"
                            else -> "Connect Apple"
                        },
                        enabled = !busy && "apple.com" !in current.providers
                    ) {
                        if (appleLinkArmed) {
                            appleLinkArmed = false
                            runAuth { manager.linkApple(activity, explicitConsent = true) }
                        } else {
                            appleLinkArmed = true
                            message = "Tap Connect Apple again to explicitly confirm provider linking."
                        }
                    }
                }
            }
            AuthCard("Account controls") {
                AuthButton("Sign out", enabled = !busy) { runAuth { manager.signOut() } }
                AuthButton(if (deleteArmed) "Confirm delete account" else "Delete account", enabled = !busy, danger = true) {
                    if (deleteArmed) runAuth { manager.deleteAccount() }
                    else {
                        deleteArmed = true
                        message = "Account deletion is permanent. Tap Delete account again to confirm."
                    }
                }
            }
        }

        message?.let {
            Surface(shape = RoundedCornerShape(18.dp), color = AuthGlass, border = androidx.compose.foundation.BorderStroke(1.dp, AuthBorder)) {
                Text(it, color = AuthIce, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(14.dp))
            }
        }
        if (busy) Text("Working…", color = AuthMint, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun AuthCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, AuthBorder, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp), color = AuthGlass
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = AuthWhite, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun AuthButton(label: String, enabled: Boolean, primary: Boolean = false, danger: Boolean = false, onClick: () -> Unit) {
    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                danger -> Color(0x55FF6F9C)
                primary -> AuthMint
                else -> AuthGlass
            }
        )
    ) {
        Text(label, color = if (primary) AuthDeep else AuthWhite, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(7.dp))
}

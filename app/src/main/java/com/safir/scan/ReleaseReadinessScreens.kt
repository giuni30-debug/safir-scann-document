package com.safir.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.io.File

private val RRWhite = Color(0xFFF9FBFF)
private val RRIce = Color(0xFFDDF8FF)
private val RRMint = Color(0xFF79FFD2)
private val RRGlass = Color(0x2EFFFFFF)
private val RRBorder = Color(0x55FFFFFF)
private val RRDeep = Color(0xFF301274)

@Composable
fun ReleaseOnboardingScreen(onContinue: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(Color(0xFF315BCB), Color(0xFF4936AE), Color(0xFF742EAF), Color(0xFF3154C4))
            )
        ).statusBarsPadding().navigationBarsPadding().padding(22.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SAFIR SCAN", color = RRWhite, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text(
                "Scan documents privately on your device",
                color = RRIce,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            ReadinessCard("Automatic edge detection", "Detect document borders and correct perspective.")
            Spacer(Modifier.height(10.dp))
            ReadinessCard("Multi-page PDF", "Capture or import several pages and save them as one PDF.")
            Spacer(Modifier.height(10.dp))
            ReadinessCard("No account required", "Your saved scans stay in the app's private storage on this device.")
            Spacer(Modifier.height(26.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RRMint)
            ) {
                Text("CONTINUE", color = RRDeep, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun ReleaseSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var cacheRevision by remember { mutableIntStateOf(0) }
    val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val draftDir = File(context.cacheDir, "scan_draft")
    val tempCount = remember(cacheRevision) { draftDir.listFiles()?.size ?: 0 }

    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(Color(0xFF315BCB), Color(0xFF4936AE), Color(0xFF742EAF), Color(0xFF3154C4))
            )
        ).statusBarsPadding().navigationBarsPadding().padding(18.dp)
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RRGlass)
                ) { Text("← Back", color = RRWhite) }
                Text(
                    "SETTINGS",
                    color = RRWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            SettingsCard("Privacy & data") {
                Text("Scanned pages and generated PDFs are processed and stored locally by Safir Scanner.", color = RRIce, fontSize = 13.sp)
                Spacer(Modifier.height(5.dp))
                Text("No account is required.", color = RRMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            SettingsCard("Camera permission") {
                Text(if (cameraGranted) "Camera access: allowed" else "Camera access: not allowed", color = if (cameraGranted) RRMint else RRWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RRGlass),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Open Android settings", color = RRWhite) }
            }

            SettingsCard("Temporary scan data") {
                Text("Temporary draft files: $tempCount", color = RRIce, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        draftDir.deleteRecursively()
                        draftDir.mkdirs()
                        cacheRevision++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RRGlass),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Clear temporary data", color = RRWhite) }
            }

            SettingsCard("About") {
                Text("Safir Scanner ${BuildConfig.VERSION_NAME}", color = RRWhite, fontWeight = FontWeight.Bold)
                Text("Build ${BuildConfig.VERSION_CODE} • Android", color = RRIce, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ReadinessCard(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, RRBorder, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = RRGlass
    ) {
        Column(Modifier.padding(17.dp)) {
            Text(title, color = RRWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))
            Text(body, color = RRIce, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, RRBorder, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = RRGlass
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = RRWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

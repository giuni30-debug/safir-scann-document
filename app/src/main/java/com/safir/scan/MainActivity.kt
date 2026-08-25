package com.safir.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val White = Color(0xFFF9FAFF)
private val Ice = Color(0xFFDAF6FF)
private val Cyan = Color(0xFF73E6FF)
private val Sapphire = Color(0xFF4E7CFF)
private val Violet = Color(0xFF7256FF)
private val Magenta = Color(0xFFD15CFF)
private val Indigo = Color(0xFF252A78)
private val DeepViolet = Color(0xFF3E168E)
private val Glass = Color(0x3DFFFFFF)
private val GlassStrong = Color(0x5AFFFFFF)
private val GlassBorder = Color(0x55FFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SafirScannerApp() }
    }
}

private enum class Screen { HOME, CAMERA }

@Composable
private fun SafirScannerApp() {
    val context = LocalContext.current
    var screen by remember { mutableStateOf(Screen.HOME) }
    var refresh by remember { mutableIntStateOf(0) }

    MaterialTheme {
        when (screen) {
            Screen.HOME -> HomeScreen(
                context = context,
                refreshKey = refresh,
                onScan = { screen = Screen.CAMERA }
            )
            Screen.CAMERA -> CameraScreen(
                onBack = { screen = Screen.HOME },
                onCaptured = {
                    refresh++
                    screen = Screen.HOME
                }
            )
        }
    }
}

@Composable
private fun HomeScreen(context: Context, refreshKey: Int, onScan: () -> Unit) {
    val files = remember(refreshKey) {
        scanDirectory(context).listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4266D7),
                        Color(0xFF4930A8),
                        Color(0xFF7A2CB8),
                        Color(0xFF314CC1)
                    )
                )
            )
    ) {
        BubbleGlow(
            modifier = Modifier.align(Alignment.TopStart).padding(top = 70.dp, start = 12.dp),
            size = 190,
            colors = listOf(Color(0x8873E6FF), Color.Transparent)
        )
        BubbleGlow(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
            size = 230,
            colors = listOf(Color(0x88D15CFF), Color.Transparent)
        )
        BubbleGlow(
            modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 30.dp),
            size = 210,
            colors = listOf(Color(0x774E7CFF), Color.Transparent)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "SAFIR SCAN",
                            color = White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Private • local • high resolution",
                            color = Ice.copy(alpha = 0.82f),
                            fontSize = 13.sp
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Glass,
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, GlassBorder, CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("S", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(330.dp)
                        .border(1.dp, GlassBorder, RoundedCornerShape(36.dp)),
                    shape = RoundedCornerShape(36.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassStrong)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0x554E7CFF),
                                        Color(0x557256FF),
                                        Color(0x44D15CFF)
                                    )
                                )
                            )
                            .padding(22.dp)
                    ) {
                        BubbleGlow(
                            modifier = Modifier.align(Alignment.TopEnd),
                            size = 165,
                            colors = listOf(Color(0xAA73E6FF), Color.Transparent)
                        )
                        BubbleGlow(
                            modifier = Modifier.align(Alignment.BottomStart),
                            size = 145,
                            colors = listOf(Color(0x99D15CFF), Color.Transparent)
                        )

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "SCAN ANYTHING",
                                    color = White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(Modifier.height(7.dp))
                                Text(
                                    "Camera fullscreen • local capture • no upload",
                                    color = Ice.copy(alpha = 0.82f),
                                    fontSize = 14.sp
                                )
                            }

                            Button(
                                onClick = onScan,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = White)
                            ) {
                                Text(
                                    "Start scan   →",
                                    color = DeepViolet,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "RECENT SCANS",
                        color = White.copy(alpha = 0.92f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Glass,
                        modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    ) {
                        Text(
                            "${files.size} local",
                            color = Ice,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (files.isEmpty()) {
                item {
                    GlassInfoCard("No scans yet. Captures stay only on this device.")
                }
            } else {
                items(files, key = { it.absolutePath }) { file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Glass)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = RoundedCornerShape(15.dp),
                                color = Color(0x55FFFFFF)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("PDF", color = White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Column(Modifier.padding(start = 14.dp)) {
                                Text(file.name, color = White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${file.length() / 1024} KB • stored locally",
                                    color = Ice.copy(alpha = 0.72f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(22.dp)) }
        }
    }
}

@Composable
private fun GlassInfoCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Glass)
    ) {
        Text(
            text,
            color = Ice.copy(alpha = 0.8f),
            modifier = Modifier.padding(18.dp),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun BubbleGlow(modifier: Modifier, size: Int, colors: List<Color>) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(colors))
    )
}

@Composable
private fun CameraScreen(onBack: () -> Unit, onCaptured: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var message by remember { mutableStateOf("Align document and hold steady") }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        granted = allowed
        if (!allowed) message = "Camera permission is required for scanning"
    }

    if (!granted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(Sapphire, DeepViolet, Magenta)))
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera access", color = White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Safir Scan uses the camera only to capture documents locally on this device.",
                    color = Ice.copy(alpha = 0.86f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = White)
                ) {
                    Text("Allow camera", color = DeepViolet, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Glass)
                ) {
                    Text("←  Back", color = White)
                }
            }
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        val provider = providerFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build()
                        imageCapture = capture
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x554E4AA8))
            ) {
                Text("←  Back", color = White, fontWeight = FontWeight.SemiBold)
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0x554E4AA8),
                modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            ) {
                Text(
                    "FULLSCREEN SCAN",
                    color = White,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.88f)
                .height(470.dp)
                .border(2.dp, Cyan.copy(alpha = 0.9f), RoundedCornerShape(28.dp))
        ) {
            Text(
                "DOCUMENT",
                color = White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .background(Color(0x665E4CE8), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xBB27358E), Color(0xBB6D2CA6), Color(0xBB2C52C3))
                    ),
                    RoundedCornerShape(30.dp)
                )
                .border(1.dp, GlassBorder, RoundedCornerShape(30.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message, color = White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val capture = imageCapture ?: return@Button
                    val file = File(scanDirectory(context), "scan_${timestamp()}.jpg")
                    val output = ImageCapture.OutputFileOptions.Builder(file).build()
                    capture.takePicture(
                        output,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                message = "Saved locally"
                                onCaptured()
                            }

                            override fun onError(exception: ImageCaptureException) {
                                message = "Capture failed: ${exception.message ?: "unknown error"}"
                            }
                        }
                    )
                },
                modifier = Modifier.size(86.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = White)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Cyan, Violet, Magenta))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("●", color = White, fontSize = 26.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Tap to capture", color = Ice.copy(alpha = 0.78f), fontSize = 11.sp)
        }
    }
}

private fun scanDirectory(context: Context): File = File(context.filesDir, "scans").apply { mkdirs() }

private fun timestamp(): String = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())

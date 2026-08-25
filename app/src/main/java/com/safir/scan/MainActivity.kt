package com.safir.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Obsidian = Color(0xFF08090D)
private val Glass = Color(0xCC171923)
private val Cyan = Color(0xFF89F7FE)
private val Violet = Color(0xFF7B61FF)
private val White = Color(0xFFF7F8FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF17122B), Obsidian),
                        radius = 1000f
                    )
                )
        ) {
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
}

@Composable
private fun HomeScreen(context: Context, refreshKey: Int, onScan: () -> Unit) {
    val files = remember(refreshKey) { scanDirectory(context).listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SAFIR SCANNER", color = White, fontSize = 18.sp)
        Text("Offline document capture", color = Color(0xFF9EA3B8), fontSize = 13.sp)
        Spacer(Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xAA89F7FE), Color(0x887B61FF), Color(0x22171923))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(142.dp)
                    .clip(CircleShape)
                    .background(Glass),
                contentAlignment = Alignment.Center
            ) {
                Text("SCAN", color = White, fontSize = 30.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onScan,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Violet)
        ) {
            Text("Scan document", fontSize = 17.sp)
        }

        Spacer(Modifier.height(12.dp))
        Text("● Offline ready", color = Cyan, fontSize = 13.sp)
        Spacer(Modifier.height(28.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Local scans", color = White, fontSize = 18.sp)
            Text("${files.size}", color = Color(0xFF9EA3B8))
        }
        Spacer(Modifier.height(10.dp))

        if (files.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Glass)
            ) {
                Text(
                    "No scans yet. Your captures stay on this device.",
                    color = Color(0xFFB9BDD0),
                    modifier = Modifier.padding(18.dp)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(files, key = { it.absolutePath }) { file ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Glass)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(file.name, color = White, fontSize = 15.sp)
                            Text("${file.length() / 1024} KB • stored locally", color = Color(0xFF9EA3B8), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraScreen(onBack: () -> Unit, onCaptured: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var message by remember { mutableStateOf("Align the document inside the frame") }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        granted = allowed
        if (!allowed) message = "Camera permission is required for real scanning"
    }

    if (!granted) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Camera access", color = White, fontSize = 24.sp)
            Spacer(Modifier.height(12.dp))
            Text("Safir Scanner uses the camera only to capture documents on this device.", color = Color(0xFFB9BDD0))
            Spacer(Modifier.height(20.dp))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Allow camera") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Glass)) { Text("Back") }
        }
        return
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        val provider = providerFuture.get()
                        val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build()
                        imageCapture = capture
                        provider.unbindAll()
                        provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.86f)
                .height(470.dp)
                .background(Color.Transparent)
        ) {
            Box(Modifier.fillMaxSize().background(Color(0x08000000)).clip(RoundedCornerShape(24.dp)))
            Text("DOCUMENT", color = Cyan, fontSize = 11.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xCC08090D)).padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message, color = White, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Glass)) { Text("Back") }
                Button(
                    onClick = {
                        val capture = imageCapture ?: return@Button
                        val file = File(scanDirectory(context), "scan_${timestamp()}.jpg")
                        val output = ImageCapture.OutputFileOptions.Builder(file).build()
                        capture.takePicture(output, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                message = "Saved locally"
                                onCaptured()
                            }
                            override fun onError(exception: ImageCaptureException) {
                                message = "Capture failed: ${exception.message ?: "unknown error"}"
                            }
                        })
                    },
                    modifier = Modifier.size(78.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = White)
                ) { Text("●", color = Obsidian, fontSize = 28.sp) }
                Spacer(Modifier.size(72.dp))
            }
        }
    }
}

private fun scanDirectory(context: Context): File = File(context.filesDir, "scans").apply { mkdirs() }

private fun timestamp(): String = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())

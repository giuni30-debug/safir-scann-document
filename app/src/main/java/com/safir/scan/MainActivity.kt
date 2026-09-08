package com.safir.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

private val White = Color(0xFFF9FAFF)
private val Ice = Color(0xFFDAF6FF)
private val Cyan = Color(0xFF73E6FF)
private val Sapphire = Color(0xFF4E7CFF)
private val Violet = Color(0xFF7256FF)
private val Magenta = Color(0xFFD15CFF)
private val DeepViolet = Color(0xFF3E168E)
private val Glass = Color(0x3DFFFFFF)
private val GlassBorder = Color(0x55FFFFFF)
private val Mint = Color(0xFF80FFD0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SafirScannerApp() }
    }
}

private enum class Screen { ONBOARDING, HOME, CAMERA, EDITOR, SETTINGS }

@Composable
private fun SafirScannerApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("safir_scan_prefs", Context.MODE_PRIVATE) }
    val recoveredDraft = remember { recoverDraftPages(context) }
    val onboardingComplete = remember { prefs.getBoolean("onboarding_complete", false) }
    var screen by remember {
        mutableStateOf(
            when {
                !onboardingComplete -> Screen.ONBOARDING
                recoveredDraft.isNotEmpty() -> Screen.CAMERA
                else -> Screen.HOME
            }
        )
    }
    var refresh by remember { mutableIntStateOf(0) }
    var draftPages by remember { mutableStateOf(recoveredDraft) }
    var savingPdf by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val pdfExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { pdfExecutor.shutdownNow() }
    }

    MaterialTheme {
        when (screen) {
            Screen.ONBOARDING -> ReleaseOnboardingScreen {
                prefs.edit().putBoolean("onboarding_complete", true).apply()
                screen = if (draftPages.isNotEmpty()) Screen.CAMERA else Screen.HOME
            }

            Screen.HOME -> Box(Modifier.fillMaxSize()) {
                PremiumHomeScreen(
                    context = context,
                    refreshKey = refresh,
                    onScan = {
                        clearDraftSession(context)
                        draftPages = emptyList()
                        saveError = null
                        screen = Screen.CAMERA
                    },
                    onDocumentDeleted = { refresh++ }
                )
                Button(
                    onClick = { screen = Screen.SETTINGS },
                    modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 10.dp, end = 14.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x66504CB0))
                ) {
                    Text("Settings", color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Screen.SETTINGS -> ReleaseSettingsScreen(onBack = { screen = Screen.HOME })

            Screen.CAMERA -> CameraScreen(
                draftPages = draftPages,
                onBack = {
                    clearDraftSession(context)
                    draftPages = emptyList()
                    saveError = null
                    screen = Screen.HOME
                },
                onPageCaptured = { draftPages = draftPages + it },
                onDeleteLast = {
                    draftPages.lastOrNull()?.let { deleteDraftPage(it) }
                    if (draftPages.isNotEmpty()) draftPages = draftPages.dropLast(1)
                },
                onFinish = {
                    if (SafirApp.hasPendingDrafts(draftPages)) {
                        false
                    } else if (draftPages.isNotEmpty()) {
                        saveError = null
                        screen = Screen.EDITOR
                        true
                    } else {
                        false
                    }
                }
            )

            Screen.EDITOR -> Box(Modifier.fillMaxSize()) {
                ScanEditorScreen(
                    pages = draftPages,
                    onBack = { if (!savingPdf) screen = Screen.CAMERA },
                    onPagesChanged = { updated ->
                        if (!savingPdf) {
                            draftPages = updated
                            if (updated.isEmpty()) screen = Screen.CAMERA
                        }
                    },
                    onSavePdf = {
                        if (!savingPdf && draftPages.isNotEmpty()) {
                            savingPdf = true
                            saveError = null
                            val pagesSnapshot = draftPages.toList()
                            pdfExecutor.execute {
                                val result = runCatching { createPdfFromImages(context, pagesSnapshot) }
                                ContextCompat.getMainExecutor(context).execute {
                                    savingPdf = false
                                    result.onSuccess {
                                        clearDraftSession(context)
                                        draftPages = emptyList()
                                        refresh++
                                        screen = Screen.HOME
                                    }.onFailure { error ->
                                        saveError = error.message ?: "PDF could not be saved. Your scan was kept."
                                    }
                                }
                            }
                        }
                    }
                )

                if (savingPdf) {
                    PdfStatusOverlay(
                        title = "Saving PDF…",
                        body = "Preparing ${draftPages.size} page(s). Keep Safir Scanner open for a moment."
                    )
                }

                saveError?.let { error ->
                    PdfStatusOverlay(
                        title = "PDF not saved",
                        body = "$error\nYour scanned pages are still available.",
                        actionLabel = "Dismiss",
                        onAction = { saveError = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfStatusOverlay(
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        Modifier.fillMaxSize().background(Color(0x880E123A)).padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xEE3E2D8F),
            modifier = Modifier.fillMaxWidth().border(1.dp, GlassBorder, RoundedCornerShape(26.dp))
        ) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, color = White, fontSize = 20.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(body, color = Ice, fontSize = 13.sp, textAlign = TextAlign.Center)
                if (actionLabel != null && onAction != null) {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onAction,
                        colors = ButtonDefaults.buttonColors(containerColor = Mint),
                        shape = RoundedCornerShape(18.dp)
                    ) { Text(actionLabel, color = DeepViolet, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun GlassPill(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Glass,
        modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
    ) {
        Text(text, color = Ice, modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CameraScreen(
    draftPages: List<File>,
    onBack: () -> Unit,
    onPageCaptured: (File) -> Unit,
    onDeleteLast: () -> Unit,
    onFinish: () -> Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var flashSupported by remember { mutableStateOf(false) }
    var torchOn by remember { mutableStateOf(false) }
    var documentDetected by remember { mutableStateOf(false) }
    var message by remember {
        mutableStateOf(
            if (draftPages.isNotEmpty()) "Recovered ${draftPages.size} draft page(s)" else "Looking for document…"
        )
    }
    var busy by remember { mutableStateOf(false) }
    var removeArmed by remember { mutableStateOf(false) }

    DisposableEffect(Unit) { onDispose { analysisExecutor.shutdownNow() } }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        granted = allowed
        if (!allowed) message = "Camera access denied • use Files or Android settings"
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        var imported = 0
        uris.forEach { uri ->
            val file = File(draftDirectory(context), "import_${timestamp()}_${imported}.jpg")
            SafirApp.markDraftPending(file)
            val importedOk = runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                file.exists() && file.length() > 0L
            }.getOrDefault(false)

            if (importedOk) {
                onPageCaptured(file)
                imported++
                removeArmed = false
            } else {
                SafirApp.clearDraftPending(file)
                file.delete()
            }
        }
        message = when {
            imported > 0 -> "$imported file(s) imported • processing pages"
            uris.isNotEmpty() -> "Import failed • choose another image"
            else -> message
        }
    }

    if (!granted) {
        Box(
            Modifier.fillMaxSize()
                .background(Brush.linearGradient(listOf(Sapphire, DeepViolet, Magenta)))
                .statusBarsPadding().navigationBarsPadding().padding(22.dp)
        ) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera access", color = White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(10.dp))
                Text("Camera is used only for local document capture. You can import images without camera access.", color = Ice, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }, colors = ButtonDefaults.buttonColors(containerColor = White)) {
                    Text("Allow camera", color = DeepViolet, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Button(onClick = { filePicker.launch(arrayOf("image/*")) }, colors = ButtonDefaults.buttonColors(containerColor = Glass)) {
                    Text("Select files", color = White)
                }
                if (draftPages.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (!onFinish()) message = "Finishing page processing… please try again in a moment"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Mint)
                    ) {
                        Text("Resume ${draftPages.size} page(s)", color = DeepViolet, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Glass)
                ) { Text("Open Android settings", color = White) }
                Spacer(Modifier.height(10.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Glass)) { Text("← Back", color = White) }
                Spacer(Modifier.height(10.dp))
                Text(message, color = Ice, textAlign = TextAlign.Center, fontSize = 12.sp)
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
                        runCatching {
                            val provider = providerFuture.get()
                            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                            val capture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build()
                            val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                                it.setAnalyzer(analysisExecutor, LiveDocumentAnalyzer { detected ->
                                    ContextCompat.getMainExecutor(ctx).execute {
                                        documentDetected = detected
                                        if (!busy) message = if (detected) "Document detected • hold steady" else "Looking for document…"
                                    }
                                })
                            }
                            imageCapture = capture
                            provider.unbindAll()
                            val bound = provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture, analysis)
                            camera = bound
                            flashSupported = bound.cameraInfo.hasFlashUnit()
                        }.onFailure {
                            imageCapture = null
                            camera = null
                            flashSupported = false
                            message = "Camera unavailable • use Files to import images"
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        )

        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0x66504CB0))) {
                Text("← Back", color = White, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = flashSupported,
                    onClick = {
                        torchOn = !torchOn
                        camera?.cameraControl?.enableTorch(torchOn)
                    },
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "Camera flash"
                        stateDescription = if (torchOn) "On" else "Off"
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x66504CB0))
                ) { Text(if (torchOn) "Flash ON" else "Flash", color = White) }
                GlassPill("${draftPages.size} page(s)")
            }
        }

        Box(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.88f).height(470.dp)
                .border(if (documentDetected) 3.dp else 2.dp, if (documentDetected) Mint else Cyan.copy(alpha = 0.9f), RoundedCornerShape(28.dp))
        ) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
                shape = RoundedCornerShape(20.dp),
                color = if (documentDetected) Color(0xAA146B65) else Color(0x665E4CE8)
            ) {
                Text(
                    if (documentDetected) "DOCUMENT DETECTED" else "SEARCHING",
                    color = White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(14.dp)
                .background(Brush.linearGradient(listOf(Color(0xD027358E), Color(0xD06D2CA6), Color(0xD02C52C3))), RoundedCornerShape(30.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(30.dp)).padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message, color = if (documentDetected) Mint else White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { filePicker.launch(arrayOf("image/*")) }, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0x5573E6FF))) {
                    Text("Files", color = White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    enabled = !busy && imageCapture != null,
                    onClick = {
                        val capture = imageCapture ?: return@Button
                        busy = true
                        removeArmed = false
                        message = "Capturing high resolution…"
                        val file = File(draftDirectory(context), "page_${timestamp()}.jpg")
                        SafirApp.markDraftPending(file)
                        capture.takePicture(
                            ImageCapture.OutputFileOptions.Builder(file).build(),
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    if (file.exists() && file.length() > 0L) {
                                        onPageCaptured(file)
                                        message = "Page ${draftPages.size + 1} saved • processing document"
                                    } else {
                                        SafirApp.clearDraftPending(file)
                                        message = "Capture failed • empty image"
                                    }
                                    busy = false
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    SafirApp.clearDraftPending(file)
                                    file.delete()
                                    message = "Capture failed: ${exception.message ?: "unknown error"}"
                                    busy = false
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .size(88.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Capture document page"
                            stateDescription = if (busy) "Capturing" else "Ready"
                        },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = White)
                ) {
                    Box(
                        Modifier.size(58.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Cyan, Violet, Magenta))),
                        contentAlignment = Alignment.Center
                    ) { Text("●", color = White, fontSize = 26.sp) }
                }
                if (draftPages.isNotEmpty()) {
                    Button(
                        onClick = {
                            if (removeArmed) {
                                onDeleteLast()
                                removeArmed = false
                                message = "Last page removed."
                            } else {
                                removeArmed = true
                                message = "Tap Confirm remove to delete the last captured page."
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x55FF7A9E))
                    ) {
                        Text(if (removeArmed) "Confirm remove" else "Remove", color = White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                } else Spacer(Modifier.size(72.dp))
            }
            if (draftPages.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        removeArmed = false
                        if (!onFinish()) message = "Finishing page processing… please try again in a moment"
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint)
                ) {
                    Text("Edit ✓  •  ${draftPages.size} page(s)", color = DeepViolet, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.height(7.dp))
            Text("Live edge detection • document processing stays on device", color = Ice.copy(alpha = 0.8f), fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}

private fun createPdfFromImages(context: Context, images: List<File>): File {
    require(images.isNotEmpty()) { "No pages to save." }

    val directory = libraryDirectory(context)
    val outputFile = File(directory, "SafirScan_${timestamp()}.pdf")
    val tempFile = File(directory, ".${outputFile.name}.tmp")
    val document = PdfDocument()
    var completed = false

    try {
        images.forEachIndexed { index, file ->
            if (!file.isFile || file.length() <= 0L) {
                throw IOException("Page ${index + 1} is missing or empty.")
            }

            val bitmap = decodeSampledBitmap(file, 2480)
                ?: throw IOException("Page ${index + 1} could not be decoded.")

            var scaled: Bitmap? = null
            try {
                if (bitmap.width <= 0 || bitmap.height <= 0) {
                    throw IOException("Page ${index + 1} has invalid dimensions.")
                }

                val ratio = bitmap.height.toFloat() / bitmap.width.toFloat()
                var pageWidth = 1240
                var pageHeight = (ratio * pageWidth).toInt().coerceAtLeast(1)
                if (pageHeight > 3508) {
                    pageHeight = 3508
                    pageWidth = (pageHeight / ratio).toInt().coerceAtLeast(1)
                }

                val page = document.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                )
                scaled = Bitmap.createScaledBitmap(bitmap, pageWidth, pageHeight, true)
                page.canvas.drawBitmap(scaled, 0f, 0f, null)
                document.finishPage(page)
            } finally {
                if (scaled != null && scaled !== bitmap && !scaled.isRecycled) scaled.recycle()
                if (!bitmap.isRecycled) bitmap.recycle()
            }
        }

        FileOutputStream(tempFile).use { output ->
            document.writeTo(output)
            output.flush()
            output.fd.sync()
        }

        if (!tempFile.isFile || tempFile.length() <= 0L) {
            throw IOException("The generated PDF is empty.")
        }

        if (!tempFile.renameTo(outputFile)) {
            tempFile.copyTo(outputFile, overwrite = false)
            tempFile.delete()
        }

        if (!outputFile.isFile || outputFile.length() <= 0L) {
            throw IOException("The PDF could not be finalized.")
        }

        completed = true
        return outputFile
    } finally {
        document.close()
        tempFile.delete()
        if (!completed) outputFile.delete()
    }
}

private fun decodeSampledBitmap(file: File, maxDimension: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > maxDimension * 2 || bounds.outHeight / sampleSize > maxDimension * 2) {
        sampleSize *= 2
    }

    return BitmapFactory.decodeFile(
        file.absolutePath,
        BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
    )
}

private fun deleteDraftPage(file: File) {
    SafirApp.clearDraftPending(file)
    file.delete()
    File(file.parentFile, ".${file.name}.safirbase.jpg").delete()
}

private fun clearDraftSession(context: Context) {
    val files = draftDirectory(context).listFiles()?.toList().orEmpty()
    SafirApp.clearPendingDrafts(files)
    files.forEach { it.delete() }
}

private fun recoverDraftPages(context: Context): List<File> =
    draftDirectory(context).listFiles()
        ?.filter { file ->
            val lower = file.name.lowercase(Locale.US)
            file.isFile &&
                file.length() > 0L &&
                !file.name.startsWith(".") &&
                (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) &&
                !lower.contains(".tmp.") &&
                !lower.contains("safirbase")
        }
        ?.sortedBy { it.lastModified() }
        .orEmpty()

private fun draftDirectory(context: Context) = File(context.cacheDir, "scan_draft").apply { mkdirs() }
private fun libraryDirectory(context: Context) = File(context.filesDir, "documents").apply { mkdirs() }
private fun timestamp() = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())

package com.safir.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.os.Bundle
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream
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
private val GlassStrong = Color(0x5AFFFFFF)
private val GlassBorder = Color(0x55FFFFFF)
private val Mint = Color(0xFF80FFD0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SafirScannerApp() }
    }
}

private enum class Screen { HOME, CAMERA, EDITOR }

@Composable
private fun SafirScannerApp() {
    val context = LocalContext.current
    var screen by remember { mutableStateOf(Screen.HOME) }
    var refresh by remember { mutableIntStateOf(0) }
    var draftPages by remember { mutableStateOf<List<File>>(emptyList()) }

    MaterialTheme {
        when (screen) {
            Screen.HOME -> HomeScreen(
                context = context,
                refreshKey = refresh,
                onScan = {
                    clearDraftSession(context)
                    draftPages = emptyList()
                    screen = Screen.CAMERA
                },
                onDocumentDeleted = { refresh++ }
            )
            Screen.CAMERA -> CameraScreen(
                draftPages = draftPages,
                onBack = {
                    clearDraftSession(context)
                    draftPages = emptyList()
                    screen = Screen.HOME
                },
                onPageCaptured = { draftPages = draftPages + it },
                onDeleteLast = {
                    draftPages.lastOrNull()?.let { deleteDraftPage(it) }
                    if (draftPages.isNotEmpty()) draftPages = draftPages.dropLast(1)
                },
                onFinish = { if (draftPages.isNotEmpty()) screen = Screen.EDITOR }
            )
            Screen.EDITOR -> ScanEditorScreen(
                pages = draftPages,
                onBack = { screen = Screen.CAMERA },
                onPagesChanged = { updated ->
                    draftPages = updated
                    if (updated.isEmpty()) screen = Screen.CAMERA
                },
                onSavePdf = {
                    if (draftPages.isNotEmpty()) {
                        createPdfFromImages(context, draftPages)
                        clearDraftSession(context)
                        draftPages = emptyList()
                        refresh++
                    }
                    screen = Screen.HOME
                }
            )
        }
    }
}

@Composable
private fun HomeScreen(context: Context, refreshKey: Int, onScan: () -> Unit, onDocumentDeleted: () -> Unit) {
    val files = remember(refreshKey) {
        libraryDirectory(context).listFiles()?.filter { it.extension.equals("pdf", true) }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(Color(0xFF315FDA), Color(0xFF4431AD), Color(0xFF812AB6), Color(0xFF3154C9))
            )
        )
    ) {
        BubbleGlow(Modifier.align(Alignment.TopStart).offset(x = (-55).dp, y = 30.dp), 260, listOf(Color(0x9973E6FF), Color.Transparent))
        BubbleGlow(Modifier.align(Alignment.CenterEnd).offset(x = 100.dp, y = (-70).dp), 300, listOf(Color(0x88D15CFF), Color.Transparent))
        BubbleGlow(Modifier.align(Alignment.BottomStart).offset(x = (-80).dp, y = 50.dp), 270, listOf(Color(0x664E7CFF), Color.Transparent))

        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        DepthTitle("SAFIR SCAN", 30)
                        Spacer(Modifier.height(3.dp))
                        Text("PRIVATE • LOCAL • INTELLIGENT", color = Ice.copy(alpha = .72f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color(0x32FFFFFF),
                        modifier = Modifier.size(50.dp).border(1.dp, Color(0x70FFFFFF), CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("S", color = White, fontSize = 19.sp, fontWeight = FontWeight.Black, style = TextStyle(shadow = Shadow(Cyan.copy(alpha = .65f), Offset.Zero, 16f)))
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(258.dp).clickable(onClick = onScan),
                    contentAlignment = Alignment.Center
                ) {
                    BubbleGlow(Modifier.align(Alignment.Center), 250, listOf(Color(0x6673E6FF), Color(0x337256FF), Color.Transparent))
                    Surface(
                        modifier = Modifier.size(214.dp).border(1.dp, Color(0x80FFFFFF), CircleShape),
                        shape = CircleShape,
                        color = Color(0x30FFFFFF)
                    ) {
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.radialGradient(listOf(Color(0x42FFFFFF), Color(0x334E7CFF), Color(0x287C38DA)))
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    modifier = Modifier.size(66.dp).border(1.dp, Color(0x88FFFFFF), RoundedCornerShape(22.dp)),
                                    shape = RoundedCornerShape(22.dp),
                                    color = Color(0x45FFFFFF)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("▣", color = White, fontSize = 27.sp, fontWeight = FontWeight.Black, style = TextStyle(shadow = Shadow(Cyan.copy(alpha = .9f), Offset.Zero, 14f)))
                                    }
                                }
                                Spacer(Modifier.height(13.dp))
                                DepthTitle("NEW SCAN", 24)
                                Spacer(Modifier.height(5.dp))
                                Text("Live edges • perspective", color = Ice.copy(alpha = .8f), fontSize = 11.sp)
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(.70f).border(1.dp, Color(0x7AFFFFFF), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xEAF7F8FF)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 15.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("START SCAN", color = DeepViolet, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            Text("   →", color = DeepViolet, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("YOUR DOCUMENTS", color = White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Text("Stored only on this device", color = Ice.copy(alpha = .6f), fontSize = 10.sp)
                    }
                    GlassPill("${files.size} PDF")
                }
            }

            if (files.isEmpty()) item { GlassInfoCard("Your saved scans will appear here.") }
            else items(files, key = { it.absolutePath }) { file ->
                DocumentCard(
                    file = file,
                    onOpen = { openPdf(context, file) },
                    onShare = { sharePdf(context, file) },
                    onDelete = { file.delete(); onDocumentDeleted() }
                )
            }
            item { Spacer(Modifier.height(22.dp)) }
        }
    }
}

@Composable
private fun DepthTitle(text: String, size: Int) {
    Box {
        Text(
            text,
            modifier = Modifier.offset(x = 1.dp, y = 3.dp),
            color = Color(0xAA321078),
            fontSize = size.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text,
            color = White,
            fontSize = size.sp,
            fontWeight = FontWeight.Black,
            style = TextStyle(shadow = Shadow(Cyan.copy(alpha = .45f), Offset(0f, 5f), 18f))
        )
    }
}

@Composable
private fun DocumentCard(file: File, onOpen: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0x6AFFFFFF), RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        color = Color(0x36FFFFFF)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(48.dp), RoundedCornerShape(16.dp), Color(0x45FFFFFF), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFFFFF))) {
                    Box(contentAlignment = Alignment.Center) { Text("PDF", color = White, fontSize = 11.sp, fontWeight = FontWeight.Black) }
                }
                Column(Modifier.padding(start = 13.dp).weight(1f)) {
                    Text(file.nameWithoutExtension, color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                    Text("${file.length() / 1024} KB • local", color = Ice.copy(alpha = .66f), fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(11.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                SmallGlassAction("Open", Modifier.weight(1f), onOpen)
                SmallGlassAction("Share", Modifier.weight(1f), onShare)
                SmallGlassAction("Delete", Modifier.weight(1f), onDelete, danger = true)
            }
        }
    }
}

@Composable
private fun SmallGlassAction(label: String, modifier: Modifier, onClick: () -> Unit, danger: Boolean = false) {
    Surface(
        modifier = modifier.clickable(onClick = onClick).border(1.dp, if (danger) Color(0x66FF9AB4) else Color(0x6573E6FF), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (danger) Color(0x32FF789D) else Color(0x3273E6FF)
    ) {
        Text(label, color = White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 10.dp))
    }
}

@Composable
private fun GlassInfoCard(text: String) {
    Surface(Modifier.fillMaxWidth().border(1.dp, GlassBorder, RoundedCornerShape(22.dp)), RoundedCornerShape(22.dp), color = Glass) {
        Text(text, color = Ice.copy(alpha = 0.8f), modifier = Modifier.padding(17.dp), fontSize = 13.sp)
    }
}

@Composable
private fun GlassPill(text: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = Glass, modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp))) {
        Text(text, color = Ice, modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BubbleGlow(modifier: Modifier, size: Int, colors: List<Color>) {
    Box(modifier.size(size.dp).clip(CircleShape).background(Brush.radialGradient(colors)))
}

@Composable
private fun CameraScreen(
    draftPages: List<File>,
    onBack: () -> Unit,
    onPageCaptured: (File) -> Unit,
    onDeleteLast: () -> Unit,
    onFinish: () -> Unit
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
    var message by remember { mutableStateOf("Looking for document…") }
    var busy by remember { mutableStateOf(false) }

    DisposableEffect(Unit) { onDispose { analysisExecutor.shutdownNow() } }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        granted = allowed
        if (!allowed) message = "Camera permission is required"
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        var imported = 0
        uris.forEach { uri ->
            try {
                val file = File(draftDirectory(context), "import_${timestamp()}_${imported}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(file).use { output -> input.copyTo(output) } }
                if (file.exists() && file.length() > 0) { onPageCaptured(file); imported++ } else file.delete()
            } catch (_: Exception) { }
        }
        if (imported > 0) message = "$imported file(s) imported • ready to edit"
    }

    if (!granted) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Sapphire, DeepViolet, Magenta))).statusBarsPadding().navigationBarsPadding().padding(22.dp)) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera access", color = White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(10.dp))
                Text("Camera is used only for local document capture.", color = Ice, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }, colors = ButtonDefaults.buttonColors(containerColor = White)) { Text("Allow camera", color = DeepViolet, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(10.dp))
                Button(onClick = { filePicker.launch(arrayOf("image/*")) }, colors = ButtonDefaults.buttonColors(containerColor = Glass)) { Text("Select files", color = White) }
                Spacer(Modifier.height(10.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Glass)) { Text("← Back", color = White) }
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
                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        )

        Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0x66504CB0))) { Text("← Back", color = White, fontWeight = FontWeight.Bold) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = flashSupported, onClick = { torchOn = !torchOn; camera?.cameraControl?.enableTorch(torchOn) }, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0x66504CB0))) { Text(if (torchOn) "⚡ ON" else "⚡", color = White) }
                GlassPill("${draftPages.size} pg")
            }
        }

        Box(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.88f).height(470.dp)
                .border(if (documentDetected) 3.dp else 2.dp, if (documentDetected) Mint else Cyan.copy(alpha = 0.9f), RoundedCornerShape(28.dp))
        ) {
            Surface(modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp), shape = RoundedCornerShape(20.dp), color = if (documentDetected) Color(0xAA146B65) else Color(0x665E4CE8)) {
                Text(if (documentDetected) "DOCUMENT DETECTED" else "SEARCHING", color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp))
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(14.dp)
                .background(Brush.linearGradient(listOf(Color(0xD027358E), Color(0xD06D2CA6), Color(0xD02C52C3))), RoundedCornerShape(30.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(30.dp)).padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message, color = if (documentDetected) Mint else White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (draftPages.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    draftPages.takeLast(6).forEachIndexed { index, _ ->
                        Surface(Modifier.size(34.dp), RoundedCornerShape(10.dp), Color(0x55FFFFFF), border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)) {
                            Box(contentAlignment = Alignment.Center) { Text("${draftPages.size - draftPages.takeLast(6).size + index + 1}", color = White, fontSize = 11.sp) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { filePicker.launch(arrayOf("image/*")) }, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0x5573E6FF))) { Text("Files", color = White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                Button(
                    enabled = !busy,
                    onClick = {
                        val capture = imageCapture ?: return@Button
                        busy = true
                        message = "Capturing high resolution…"
                        val file = File(draftDirectory(context), "page_${timestamp()}.jpg")
                        capture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) { onPageCaptured(file); message = "Page ${draftPages.size + 1} saved • OpenCV processing"; busy = false }
                            override fun onError(exception: ImageCaptureException) { message = "Capture failed: ${exception.message ?: "unknown error"}"; busy = false }
                        })
                    },
                    modifier = Modifier.size(88.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = White)
                ) {
                    Box(Modifier.size(58.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Cyan, Violet, Magenta))), contentAlignment = Alignment.Center) { Text("●", color = White, fontSize = 26.sp) }
                }
                if (draftPages.isNotEmpty()) Button(onClick = onDeleteLast, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0x55FF7A9E))) { Text("Remove", color = White, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                else Spacer(Modifier.size(72.dp))
            }
            if (draftPages.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Button(onClick = onFinish, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = Mint)) {
                    Text("Edit ✓  •  ${draftPages.size} page(s)", color = DeepViolet, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.height(7.dp))
            Text("Live edge detection • local processing • no upload", color = Ice.copy(alpha = 0.8f), fontSize = 11.sp)
        }
    }
}

private fun openPdf(context: Context, file: File) {
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val intent = Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    try { context.startActivity(Intent.createChooser(intent, "Open PDF")) } catch (_: Exception) { }
}

private fun sharePdf(context: Context, file: File) {
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val intent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    try { context.startActivity(Intent.createChooser(intent, "Share PDF")) } catch (_: Exception) { }
}

private fun createPdfFromImages(context: Context, images: List<File>): File {
    val outputFile = File(libraryDirectory(context), "SafirScan_${timestamp()}.pdf")
    val document = PdfDocument()
    try {
        images.forEachIndexed { index, file ->
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@forEachIndexed
            val pageWidth = 1240
            val pageHeight = ((bitmap.height.toFloat() / bitmap.width.toFloat()) * pageWidth).toInt().coerceAtLeast(1)
            val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create())
            val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, pageWidth, pageHeight, true)
            page.canvas.drawBitmap(scaled, 0f, 0f, null)
            document.finishPage(page)
            if (scaled !== bitmap) scaled.recycle()
            bitmap.recycle()
        }
        FileOutputStream(outputFile).use { document.writeTo(it) }
    } finally { document.close() }
    return outputFile
}

private fun deleteDraftPage(file: File) {
    file.delete()
    File(file.parentFile, ".${file.name}.safirbase.jpg").delete()
}

private fun clearDraftSession(context: Context) {
    draftDirectory(context).listFiles()?.forEach { it.delete() }
}

private fun draftDirectory(context: Context) = File(context.cacheDir, "scan_draft").apply { mkdirs() }
private fun libraryDirectory(context: Context) = File(context.filesDir, "documents").apply { mkdirs() }
private fun timestamp() = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())

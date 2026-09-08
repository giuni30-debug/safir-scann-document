package com.safir.scan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OcrActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                OcrEditorScreen(onBack = { finish() })
            }
        }
    }
}

private val OcrWhite = Color(0xFFF9FBFF)
private val OcrIce = Color(0xFFDDF8FF)
private val OcrMint = Color(0xFF79FFD2)
private val OcrGlass = Color(0x33FFFFFF)
private val OcrBorder = Color(0x55FFFFFF)
private val OcrDeep = Color(0xFF301274)

private const val MAX_OCR_PAGES = 50
private const val MAX_OCR_FILE_BYTES = 25L * 1024L * 1024L

@Composable
private fun OcrEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pages by remember { mutableStateOf<List<File>>(emptyList()) }
    var selected by remember { mutableIntStateOf(0) }
    var recognizedText by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Select up to $MAX_OCR_PAGES images. OCR runs on-device with the bundled Latin-script model.") }

    fun runRecognition(files: List<File>) {
        if (files.isEmpty() || busy) return
        busy = true
        message = "Recognizing text on ${files.size} page(s)…"
        scope.launch {
            val result = runCatching { recognizePages(context, files) }
            busy = false
            result.onSuccess { text ->
                recognizedText = text
                message = if (text.isBlank()) {
                    "No text was recognized. Try a clearer image or edit the text manually."
                } else {
                    "OCR complete. Review and edit the text before export."
                }
            }.onFailure { error ->
                message = error.message ?: "OCR could not be completed."
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty() || busy) return@rememberLauncherForActivityResult
        busy = true
        message = "Importing images…"
        scope.launch {
            val importResult = withContext(Dispatchers.IO) { importOcrImages(context, uris) }
            pages = importResult.files
            selected = 0
            recognizedText = ""
            busy = false
            message = when {
                importResult.files.isEmpty() -> "No supported image could be imported. Files may be too large or unreadable."
                importResult.skipped > 0 -> "Imported ${importResult.files.size} page(s); skipped ${importResult.skipped}. Tap Recognize text."
                else -> "Imported ${importResult.files.size} page(s). Tap Recognize text."
            }
        }
    }

    val textExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri == null || recognizedText.isBlank()) return@rememberLauncherForActivityResult
        scope.launch {
            val success = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri, "w")?.use { output ->
                        output.write(recognizedText.toByteArray(Charsets.UTF_8))
                        output.flush()
                    } ?: error("Output destination is unavailable.")
                }.isSuccess
            }
            message = if (success) "TXT exported successfully." else "TXT export failed. Your edited text is still here."
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
                    colors = ButtonDefaults.buttonColors(containerColor = OcrGlass),
                    shape = RoundedCornerShape(18.dp)
                ) { Text("← Back", color = OcrWhite) }
                Text(
                    "OCR & TEXT",
                    color = OcrWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            OcrCard("Local OCR") {
                Text(
                    "Document images stay on this device during recognition. The bundled Latin-script model is available without a model download.",
                    color = OcrIce,
                    fontSize = 12.sp
                )
                Spacer(Modifier.padding(top = 9.dp))
                Button(
                    enabled = !busy,
                    onClick = { imagePicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OcrMint),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Select images", color = OcrDeep, fontWeight = FontWeight.Black) }
                if (pages.isNotEmpty()) {
                    Spacer(Modifier.padding(top = 8.dp))
                    Button(
                        enabled = !busy,
                        onClick = { runRecognition(pages) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OcrGlass),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Recognize text • ${pages.size} page(s)", color = OcrWhite) }
                }
            }

            if (pages.isNotEmpty()) {
                OcrCard("Image export") {
                    Text("Selected page: ${selected + 1} / ${pages.size}", color = OcrIce, fontSize = 12.sp)
                    Spacer(Modifier.padding(top = 7.dp))
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        pages.forEachIndexed { index, _ ->
                            Button(
                                enabled = !busy,
                                onClick = { selected = index },
                                colors = ButtonDefaults.buttonColors(containerColor = if (index == selected) OcrMint else OcrGlass),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("${index + 1}", color = if (index == selected) OcrDeep else OcrWhite) }
                        }
                    }
                    Spacer(Modifier.padding(top = 8.dp))
                    Button(
                        enabled = !busy && pages.getOrNull(selected) != null,
                        onClick = {
                            val page = pages.getOrNull(selected) ?: return@Button
                            runCatching { shareOcrImage(context, page) }
                                .onFailure { message = "Image could not be shared." }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OcrGlass),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Share selected image", color = OcrWhite) }
                }
            }

            OcrCard("Editable text") {
                OutlinedTextField(
                    value = recognizedText,
                    onValueChange = { recognizedText = it },
                    enabled = !busy,
                    label = { Text("Recognized text") },
                    placeholder = { Text("Recognized text appears here and remains editable before export.") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp)
                )
                Spacer(Modifier.padding(top = 9.dp))
                Button(
                    enabled = !busy && recognizedText.isNotBlank(),
                    onClick = { shareOcrText(context, recognizedText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OcrGlass),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Share text", color = OcrWhite) }
                Spacer(Modifier.padding(top = 7.dp))
                Button(
                    enabled = !busy && recognizedText.isNotBlank(),
                    onClick = { textExporter.launch("SafirScan_OCR_${ocrTimestamp()}.txt") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OcrMint),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Export TXT", color = OcrDeep, fontWeight = FontWeight.Black) }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, OcrBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = OcrGlass
            ) {
                Text(
                    if (busy) "Working…" else message,
                    color = if (busy) OcrMint else OcrIce,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(Modifier.padding(top = 20.dp))
        }
    }
}

@Composable
private fun OcrCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, OcrBorder, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = OcrGlass
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = OcrWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.padding(top = 8.dp))
            content()
        }
    }
}

private data class OcrImportResult(val files: List<File>, val skipped: Int)

private fun importOcrImages(context: Context, sourceUris: List<Uri>): OcrImportResult {
    val directory = File(context.cacheDir, "ocr_imports").apply {
        deleteRecursively()
        mkdirs()
    }
    val files = mutableListOf<File>()
    var skipped = 0

    sourceUris.take(MAX_OCR_PAGES).forEachIndexed { index, uri ->
        val mime = context.contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "img"
        val output = File(directory, "page_${index + 1}.$extension")
        val copied = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(output).use { stream ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > MAX_OCR_FILE_BYTES) throw IOException("Image is larger than the OCR limit.")
                        stream.write(buffer, 0, read)
                    }
                    stream.flush()
                    stream.fd.sync()
                }
            } ?: error("Image could not be opened.")
            output.isFile && output.length() > 0L
        }.getOrDefault(false)

        if (copied) files += output else {
            output.delete()
            skipped++
        }
    }

    if (sourceUris.size > MAX_OCR_PAGES) skipped += sourceUris.size - MAX_OCR_PAGES
    return OcrImportResult(files, skipped)
}

private suspend fun recognizePages(context: Context, pages: List<File>): String {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    return try {
        buildString {
            pages.forEachIndexed { index, file ->
                if (!file.isFile || file.length() <= 0L) throw IOException("Page ${index + 1} is missing or empty.")
                val image = InputImage.fromFilePath(context, Uri.fromFile(file))
                val result = recognizer.process(image).awaitOcr()
                if (index > 0) append("\n\n")
                append("--- Page ${index + 1} ---\n")
                append(result.text.trim())
            }
        }
    } finally {
        recognizer.close()
    }
}

private fun shareOcrText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share recognized text"))
}

private fun shareOcrImage(context: Context, source: File) {
    val directory = File(context.cacheDir, "share_exports").apply {
        deleteRecursively()
        mkdirs()
    }
    val extension = source.extension.ifBlank { "jpg" }
    val export = File(directory, "SafirScan_image_${ocrTimestamp()}.$extension")
    source.copyTo(export, overwrite = true)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", export)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share scanned image"))
}

private fun ocrTimestamp(): String =
    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

private suspend fun <T> Task<T>.awaitOcr(): T =
    suspendCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) }
        addOnFailureListener { continuation.resumeWithException(it) }
    }

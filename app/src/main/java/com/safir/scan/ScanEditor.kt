package com.safir.scan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

private val EditorWhite = Color(0xFFF9FBFF)
private val EditorIce = Color(0xFFDDF8FF)
private val EditorCyan = Color(0xFF74EAFF)
private val EditorMint = Color(0xFF79FFD2)
private val EditorGlass = Color(0x42FFFFFF)
private val EditorBorder = Color(0x60FFFFFF)

enum class ScanFilter(val label: String) {
    ORIGINAL("Original"),
    COLOR_PLUS("Color+"),
    GRAYSCALE("Gray"),
    BLACK_WHITE("B&W"),
    HIGH_CONTRAST("Contrast")
}

@Composable
fun ScanEditorScreen(
    pages: List<File>,
    onBack: () -> Unit,
    onPagesChanged: (List<File>) -> Unit,
    onSavePdf: () -> Unit
) {
    val context = LocalContext.current
    val worker = remember { Executors.newSingleThreadExecutor() }
    var selected by remember(pages.size) { mutableIntStateOf(0) }
    var cropTarget by remember { mutableStateOf<File?>(null) }
    var revision by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf(ScanFilter.ORIGINAL) }
    var busy by remember { mutableStateOf(false) }
    var busyLabel by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var deleteArmed by remember { mutableStateOf<File?>(null) }

    DisposableEffect(Unit) {
        onDispose { worker.shutdownNow() }
    }

    fun runEditorTask(label: String, work: () -> Unit, onSuccess: () -> Unit) {
        if (busy) return
        busy = true
        busyLabel = label
        error = null
        worker.execute {
            val result = runCatching(work)
            ContextCompat.getMainExecutor(context).execute {
                busy = false
                busyLabel = ""
                result.onSuccess { onSuccess() }
                    .onFailure { failure -> error = failure.message ?: "Edit failed. Original page was kept." }
            }
        }
    }

    cropTarget?.let { target ->
        ManualCropScreen(
            file = target,
            onCancel = { cropTarget = null },
            onApplied = {
                runEditorTask(
                    label = "Finalizing crop…",
                    work = { commitCurrentAsBase(target) },
                    onSuccess = {
                        selectedFilter = ScanFilter.ORIGINAL
                        cropTarget = null
                        revision++
                        onPagesChanged(pages.toList())
                    }
                )
            }
        )
        return
    }

    val safeSelected = selected.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
    val current = pages.getOrNull(safeSelected)
    val preview = remember(current?.absolutePath, current?.lastModified(), revision) {
        current?.let { decodeEditorPreview(it) }
    }

    DisposableEffect(preview) {
        onDispose {
            if (preview != null && !preview.isRecycled) preview.recycle()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF3459CF), Color(0xFF5034B0), Color(0xFF812FB8), Color(0xFF3157C8)))
        )
    ) {
        Box(
            Modifier.align(Alignment.TopEnd).padding(top = 84.dp, end = 6.dp).size(230.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0x8874EAFF), Color.Transparent)))
        )

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                GlassAction("← Back", enabled = !busy, onClick = onBack)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EDIT SCAN", color = EditorWhite, fontSize = 17.sp, fontWeight = FontWeight.Black)
                    Text("${safeSelected + 1} / ${pages.size}", color = EditorIce.copy(alpha = .75f), fontSize = 11.sp)
                }
                Surface(shape = RoundedCornerShape(18.dp), color = EditorGlass, modifier = Modifier.border(1.dp, EditorBorder, RoundedCornerShape(18.dp))) {
                    Text("LOCAL", color = EditorMint, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp))
                }
            }

            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f).border(1.dp, EditorBorder, RoundedCornerShape(30.dp)),
                shape = RoundedCornerShape(30.dp),
                color = Color(0x26FFFFFF)
            ) {
                Box(Modifier.fillMaxSize().padding(9.dp), contentAlignment = Alignment.Center) {
                    if (preview != null) {
                        Image(
                            bitmap = preview.asImageBitmap(),
                            contentDescription = "Scanned document page ${safeSelected + 1} of ${pages.size}",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("Page preview unavailable", color = EditorIce)
                    }
                }
            }

            if (busy || error != null || deleteArmed != null) {
                Spacer(Modifier.height(7.dp))
                Text(
                    when {
                        busy -> busyLabel
                        error != null -> error!!
                        deleteArmed != null -> "Tap Confirm delete to remove this page."
                        else -> ""
                    },
                    color = if (error != null) Color(0xFFFFD8E3) else EditorMint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                pages.forEachIndexed { index, _ ->
                    val active = index == safeSelected
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .semantics {
                                role = Role.Button
                                selected = active
                                contentDescription = "Page ${index + 1} of ${pages.size}"
                            }
                            .clickable(enabled = !busy) {
                                selected = index
                                selectedFilter = ScanFilter.ORIGINAL
                                deleteArmed = null
                                error = null
                            }
                            .border(if (active) 2.dp else 1.dp, if (active) EditorMint else EditorBorder, RoundedCornerShape(13.dp)),
                        shape = RoundedCornerShape(13.dp),
                        color = if (active) Color(0x5579FFD2) else EditorGlass
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${index + 1}", color = EditorWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                ToolButton("Crop", enabled = !busy) {
                    current?.let { page ->
                        runEditorTask(
                            label = "Preparing crop…",
                            work = {
                                ensureBase(page)
                                restoreBase(page)
                            },
                            onSuccess = {
                                selectedFilter = ScanFilter.ORIGINAL
                                revision++
                                deleteArmed = null
                                cropTarget = page
                            }
                        )
                    }
                }

                ToolButton("Rotate", enabled = !busy) {
                    current?.let { page ->
                        runEditorTask(
                            label = "Rotating page…",
                            work = {
                                ensureBase(page)
                                rotateFile90(baseFile(page))
                                restoreBase(page)
                            },
                            onSuccess = {
                                selectedFilter = ScanFilter.ORIGINAL
                                deleteArmed = null
                                revision++
                                onPagesChanged(pages.toList())
                            }
                        )
                    }
                }

                if (pages.size > 1) {
                    ToolButton("Move page left", enabled = !busy) {
                        if (safeSelected > 0) {
                            val next = pages.toMutableList()
                            val item = next.removeAt(safeSelected)
                            next.add(safeSelected - 1, item)
                            selected = safeSelected - 1
                            selectedFilter = ScanFilter.ORIGINAL
                            deleteArmed = null
                            onPagesChanged(next)
                        }
                    }
                    ToolButton("Move page right", enabled = !busy) {
                        if (safeSelected < pages.lastIndex) {
                            val next = pages.toMutableList()
                            val item = next.removeAt(safeSelected)
                            next.add(safeSelected + 1, item)
                            selected = safeSelected + 1
                            selectedFilter = ScanFilter.ORIGINAL
                            deleteArmed = null
                            onPagesChanged(next)
                        }
                    }
                }

                ToolButton(
                    label = if (deleteArmed == current && current != null) "Confirm delete" else "Delete page",
                    enabled = !busy
                ) {
                    val page = current ?: return@ToolButton
                    if (deleteArmed != page) {
                        deleteArmed = page
                    } else {
                        runEditorTask(
                            label = "Deleting page…",
                            work = {
                                baseFile(page).delete()
                                if (!page.delete() && page.exists()) error("Page could not be deleted.")
                            },
                            onSuccess = {
                                val next = pages.toMutableList().apply {
                                    if (safeSelected in indices) removeAt(safeSelected)
                                }
                                deleteArmed = null
                                selected = selected.coerceAtMost((next.size - 1).coerceAtLeast(0))
                                revision++
                                onPagesChanged(next)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                ScanFilter.entries.forEach { filter ->
                    FilterButton(filter.label, selectedFilter == filter, enabled = !busy) {
                        current?.let { page ->
                            runEditorTask(
                                label = "Applying ${filter.label}…",
                                work = { applyFilterFromBase(page, filter) },
                                onSuccess = {
                                    selectedFilter = filter
                                    deleteArmed = null
                                    revision++
                                    onPagesChanged(pages.toList())
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Button(
                enabled = pages.isNotEmpty() && !busy && !SafirApp.hasPendingDrafts(pages),
                onClick = {
                    runCatching { context.startActivity(ocrIntentForPages(context, pages.toList())) }
                        .onFailure { error = "OCR screen could not be opened." }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EditorGlass)
            ) {
                Text("OCR & TEXT  •  ${pages.size} page(s)", color = EditorWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(7.dp))
            Button(
                enabled = pages.isNotEmpty() && !busy,
                onClick = onSavePdf,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EditorMint)
            ) {
                Text("Save PDF  •  ${pages.size} page(s)", color = Color(0xFF301274), fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun GlassAction(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .semantics {
                role = Role.Button
                contentDescription = label
            }
            .clickable(enabled = enabled, onClick = onClick)
            .border(1.dp, EditorBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = EditorGlass
    ) {
        Text(
            label,
            color = EditorWhite.copy(alpha = if (enabled) 1f else .45f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun ToolButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .semantics {
                role = Role.Button
                contentDescription = label
            }
            .clickable(enabled = enabled, onClick = onClick)
            .border(1.dp, EditorBorder, RoundedCornerShape(17.dp)),
        shape = RoundedCornerShape(17.dp),
        color = Color(0x42FFFFFF)
    ) {
        Text(
            label,
            color = EditorWhite.copy(alpha = if (enabled) 1f else .45f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun FilterButton(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .semantics {
                role = Role.Button
                this.selected = selected
                contentDescription = "$label filter"
            }
            .clickable(enabled = enabled, onClick = onClick)
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) EditorMint else EditorCyan.copy(alpha = .55f),
                RoundedCornerShape(17.dp)
            ),
        shape = RoundedCornerShape(17.dp),
        color = if (selected) Color(0x5579FFD2) else Color(0x3574EAFF)
    ) {
        Text(
            label,
            color = EditorWhite.copy(alpha = if (enabled) 1f else .45f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        )
    }
}

private fun baseFile(file: File) = File(file.parentFile, ".${file.name}.safirbase.jpg")

private fun ensureBase(file: File) {
    val base = baseFile(file)
    if (!base.exists() || base.length() == 0L) file.copyTo(base, overwrite = true)
}

private fun restoreBase(file: File) {
    val base = baseFile(file)
    if (base.exists() && base.length() > 0L) base.copyTo(file, overwrite = true)
    file.setLastModified(System.currentTimeMillis())
}

private fun commitCurrentAsBase(file: File) {
    file.copyTo(baseFile(file), overwrite = true)
    file.setLastModified(System.currentTimeMillis())
}

private fun decodeEditorPreview(file: File, maxDimension: Int = 1800): Bitmap? =
    decodeWorkingBitmap(file, maxDimension)

private fun decodeWorkingBitmap(file: File, maxDimension: Int): Bitmap? {
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

private fun rotateFile90(file: File) {
    val source = decodeWorkingBitmap(file, 4096) ?: error("Page could not be decoded for rotation.")
    try {
        val matrix = Matrix().apply { postRotate(90f) }
        val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        try {
            saveJpeg(rotated, file)
        } finally {
            if (rotated !== source && !rotated.isRecycled) rotated.recycle()
        }
    } finally {
        if (!source.isRecycled) source.recycle()
    }
}

private fun applyFilterFromBase(file: File, filter: ScanFilter) {
    ensureBase(file)
    if (filter == ScanFilter.ORIGINAL) {
        restoreBase(file)
        return
    }

    val source = decodeWorkingBitmap(baseFile(file), 3000)
        ?: error("Page could not be decoded for filtering.")
    try {
        val result = when (filter) {
            ScanFilter.ORIGINAL -> source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
            ScanFilter.COLOR_PLUS -> colorMatrixBitmap(
                source,
                ColorMatrix(
                    floatArrayOf(
                        1.16f, 0f, 0f, 0f, 3f,
                        0f, 1.12f, 0f, 0f, 3f,
                        0f, 0f, 1.08f, 0f, 3f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            ScanFilter.GRAYSCALE -> colorMatrixBitmap(source, ColorMatrix().apply { setSaturation(0f) })
            ScanFilter.HIGH_CONTRAST -> colorMatrixBitmap(source, contrastMatrix(1.35f))
            ScanFilter.BLACK_WHITE -> shadowSafeBlackWhite(source)
        }
        try {
            saveJpeg(result, file)
        } finally {
            if (!result.isRecycled) result.recycle()
        }
    } finally {
        if (!source.isRecycled) source.recycle()
    }
}

private fun colorMatrixBitmap(source: Bitmap, matrix: ColorMatrix): Bitmap {
    val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { colorFilter = ColorMatrixColorFilter(matrix) }
    Canvas(out).drawBitmap(source, 0f, 0f, paint)
    return out
}

private fun contrastMatrix(contrast: Float): ColorMatrix {
    val translate = (-.5f * contrast + .5f) * 255f
    return ColorMatrix(
        floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
    )
}

private fun shadowSafeBlackWhite(source: Bitmap): Bitmap {
    if (!OpenCVLoader.initLocal()) return colorMatrixBitmap(source, ColorMatrix().apply { setSaturation(0f) })

    val rgba = Mat()
    val gray = Mat()
    val background = Mat()
    val normalized = Mat()
    val bw = Mat()
    return try {
        Utils.bitmapToMat(source, rgba)
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.GaussianBlur(gray, background, Size(41.0, 41.0), 0.0)
        Core.divide(gray, background, normalized, 255.0)
        Imgproc.threshold(normalized, bw, 0.0, 255.0, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU)
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        Imgproc.cvtColor(bw, rgba, Imgproc.COLOR_GRAY2RGBA)
        Utils.matToBitmap(rgba, out)
        out
    } catch (_: Throwable) {
        colorMatrixBitmap(source, ColorMatrix().apply { setSaturation(0f) })
    } finally {
        rgba.release()
        gray.release()
        background.release()
        normalized.release()
        bw.release()
    }
}

private fun saveJpeg(bitmap: Bitmap, file: File) {
    val temp = File(file.parentFile, file.name + ".edit.tmp")
    val written = FileOutputStream(temp).use { output ->
        val ok = bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
        output.flush()
        output.fd.sync()
        ok
    }

    if (!written || temp.length() <= 0L) {
        temp.delete()
        error("Edited page could not be written.")
    }

    if (!temp.renameTo(file)) {
        temp.copyTo(file, overwrite = true)
        temp.delete()
    }
    file.setLastModified(System.currentTimeMillis())
}

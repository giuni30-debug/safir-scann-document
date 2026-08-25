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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream

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
    var selected by remember(pages.size) { mutableIntStateOf(0) }
    var cropTarget by remember { mutableStateOf<File?>(null) }
    var revision by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf(ScanFilter.ORIGINAL) }

    LaunchedEffect(pages.map { it.absolutePath }) {
        pages.forEach { ensureBase(it) }
    }

    cropTarget?.let { target ->
        ManualCropScreen(
            file = target,
            onCancel = { cropTarget = null },
            onApplied = {
                commitCurrentAsBase(target)
                selectedFilter = ScanFilter.ORIGINAL
                cropTarget = null
                revision++
                onPagesChanged(pages.toList())
            }
        )
        return
    }

    val safeSelected = selected.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
    val current = pages.getOrNull(safeSelected)
    val preview = remember(current?.absolutePath, revision) {
        current?.let { BitmapFactory.decodeFile(it.absolutePath) }
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
                GlassAction("← Back", onBack)
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
                shape = RoundedCornerShape(30.dp), color = Color(0x26FFFFFF)
            ) {
                Box(Modifier.fillMaxSize().padding(9.dp), contentAlignment = Alignment.Center) {
                    if (preview != null) Image(
                        bitmap = preview.asImageBitmap(), contentDescription = "Scanned page",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)), contentScale = ContentScale.Fit
                    ) else Text("No page selected", color = EditorIce)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                pages.forEachIndexed { index, _ ->
                    val active = index == safeSelected
                    Surface(
                        modifier = Modifier.size(44.dp).clickable { selected = index; selectedFilter = ScanFilter.ORIGINAL }
                            .border(if (active) 2.dp else 1.dp, if (active) EditorMint else EditorBorder, RoundedCornerShape(13.dp)),
                        shape = RoundedCornerShape(13.dp), color = if (active) Color(0x5579FFD2) else EditorGlass
                    ) { Box(contentAlignment = Alignment.Center) { Text("${index + 1}", color = EditorWhite, fontWeight = FontWeight.Bold) } }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                ToolButton("Crop") {
                    current?.let {
                        ensureBase(it)
                        restoreBase(it)
                        selectedFilter = ScanFilter.ORIGINAL
                        revision++
                        cropTarget = it
                    }
                }
                ToolButton("Rotate") {
                    current?.let {
                        ensureBase(it)
                        rotateFile90(baseFile(it))
                        restoreBase(it)
                        selectedFilter = ScanFilter.ORIGINAL
                        revision++
                        onPagesChanged(pages.toList())
                    }
                }
                if (pages.size > 1) {
                    ToolButton("← Page") {
                        if (safeSelected > 0) {
                            val next = pages.toMutableList(); val item = next.removeAt(safeSelected); next.add(safeSelected - 1, item)
                            selected = safeSelected - 1; selectedFilter = ScanFilter.ORIGINAL; onPagesChanged(next)
                        }
                    }
                    ToolButton("Page →") {
                        if (safeSelected < pages.lastIndex) {
                            val next = pages.toMutableList(); val item = next.removeAt(safeSelected); next.add(safeSelected + 1, item)
                            selected = safeSelected + 1; selectedFilter = ScanFilter.ORIGINAL; onPagesChanged(next)
                        }
                    }
                }
                ToolButton("Delete") {
                    current?.let { baseFile(it).delete(); it.delete() }
                    val next = pages.toMutableList().apply { if (safeSelected in indices) removeAt(safeSelected) }
                    selected = selected.coerceAtMost((next.size - 1).coerceAtLeast(0)); revision++; onPagesChanged(next)
                }
            }

            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                ScanFilter.entries.forEach { filter ->
                    FilterButton(filter.label, selectedFilter == filter) {
                        current?.let {
                            applyFilterFromBase(it, filter)
                            selectedFilter = filter
                            revision++
                            onPagesChanged(pages.toList())
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Button(
                enabled = pages.isNotEmpty(), onClick = onSavePdf,
                modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EditorMint)
            ) {
                Text("Save PDF  •  ${pages.size} page(s)", color = Color(0xFF301274), fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun GlassAction(label: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.clickable(onClick = onClick).border(1.dp, EditorBorder, RoundedCornerShape(18.dp)), shape = RoundedCornerShape(18.dp), color = EditorGlass) {
        Text(label, color = EditorWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp))
    }
}

@Composable
private fun ToolButton(label: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.clickable(onClick = onClick).border(1.dp, EditorBorder, RoundedCornerShape(17.dp)), shape = RoundedCornerShape(17.dp), color = Color(0x42FFFFFF)) {
        Text(label, color = EditorWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
    }
}

@Composable
private fun FilterButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick).border(if (selected) 2.dp else 1.dp, if (selected) EditorMint else EditorCyan.copy(alpha = .55f), RoundedCornerShape(17.dp)),
        shape = RoundedCornerShape(17.dp), color = if (selected) Color(0x5579FFD2) else Color(0x3574EAFF)
    ) {
        Text(label, color = EditorWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
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

private fun rotateFile90(file: File) {
    val source = BitmapFactory.decodeFile(file.absolutePath) ?: return
    val matrix = Matrix().apply { postRotate(90f) }
    val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    saveJpeg(rotated, file)
    if (rotated !== source) rotated.recycle()
    source.recycle()
}

private fun applyFilterFromBase(file: File, filter: ScanFilter) {
    ensureBase(file)
    if (filter == ScanFilter.ORIGINAL) { restoreBase(file); return }
    val source = BitmapFactory.decodeFile(baseFile(file).absolutePath) ?: return
    val result = when (filter) {
        ScanFilter.ORIGINAL -> source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
        ScanFilter.COLOR_PLUS -> colorMatrixBitmap(source, ColorMatrix(floatArrayOf(
            1.16f, 0f, 0f, 0f, 3f,
            0f, 1.12f, 0f, 0f, 3f,
            0f, 0f, 1.08f, 0f, 3f,
            0f, 0f, 0f, 1f, 0f
        )))
        ScanFilter.GRAYSCALE -> colorMatrixBitmap(source, ColorMatrix().apply { setSaturation(0f) })
        ScanFilter.HIGH_CONTRAST -> colorMatrixBitmap(source, contrastMatrix(1.35f))
        ScanFilter.BLACK_WHITE -> thresholdBitmap(source)
    }
    saveJpeg(result, file); result.recycle(); source.recycle()
}

private fun colorMatrixBitmap(source: Bitmap, matrix: ColorMatrix): Bitmap {
    val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { colorFilter = ColorMatrixColorFilter(matrix) }
    Canvas(out).drawBitmap(source, 0f, 0f, paint)
    return out
}

private fun contrastMatrix(contrast: Float): ColorMatrix {
    val translate = (-.5f * contrast + .5f) * 255f
    return ColorMatrix(floatArrayOf(
        contrast, 0f, 0f, 0f, translate,
        0f, contrast, 0f, 0f, translate,
        0f, 0f, contrast, 0f, translate,
        0f, 0f, 0f, 1f, 0f
    ))
}

private fun thresholdBitmap(source: Bitmap): Bitmap {
    val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val pixels = IntArray(source.width * source.height)
    source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
    for (i in pixels.indices) {
        val c = pixels[i]
        val gray = (android.graphics.Color.red(c) * .299 + android.graphics.Color.green(c) * .587 + android.graphics.Color.blue(c) * .114).toInt()
        val v = if (gray >= 155) 255 else 0
        pixels[i] = android.graphics.Color.rgb(v, v, v)
    }
    out.setPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
    return out
}

private fun saveJpeg(bitmap: Bitmap, file: File) {
    val temp = File(file.parentFile, file.name + ".edit.tmp")
    FileOutputStream(temp).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
    if (temp.length() > 0L) {
        if (!temp.renameTo(file)) { temp.copyTo(file, overwrite = true); temp.delete() }
        file.setLastModified(System.currentTimeMillis())
    }
}

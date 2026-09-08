package com.safir.scan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import java.util.concurrent.Executors

private data class CropPoint(val x: Float, val y: Float)
private val fullQuad = listOf(CropPoint(0f, 0f), CropPoint(1f, 0f), CropPoint(1f, 1f), CropPoint(0f, 1f))

@Composable
fun ManualCropScreen(file: File, onCancel: () -> Unit, onApplied: () -> Unit) {
    val context = LocalContext.current
    val cropExecutor = remember { Executors.newSingleThreadExecutor() }
    val bitmap = remember(file.absolutePath, file.lastModified()) { decodeCropPreview(file) }
    var quad by remember(file.absolutePath) { mutableStateOf(fullQuad) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var dragging by remember { mutableIntStateOf(-1) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { cropExecutor.shutdownNow() }
    }

    fun layout(): FloatArray? {
        if (bitmap == null || boxSize.width <= 0 || boxSize.height <= 0) return null
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        val scale = minOf(boxSize.width / bw, boxSize.height / bh)
        val w = bw * scale
        val h = bh * scale
        return floatArrayOf((boxSize.width - w) / 2f, (boxSize.height - h) / 2f, w, h)
    }

    fun toScreen(p: CropPoint, l: FloatArray) = Offset(l[0] + p.x * l[2], l[1] + p.y * l[3])

    fun updatePoint(index: Int, pos: Offset) {
        if (busy) return
        val l = layout() ?: return
        val nx = ((pos.x - l[0]) / l[2]).coerceIn(0f, 1f)
        val ny = ((pos.y - l[1]) / l[3]).coerceIn(0f, 1f)
        quad = quad.mapIndexed { i, p -> if (i == index) CropPoint(nx, ny) else p }
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(Color(0xFF294EC8), Color(0xFF5135B5), Color(0xFF8B2FB6), Color(0xFF315DCC))
            )
        )
    ) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassCropAction("← Cancel", enabled = !busy, onClick = onCancel)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MANUAL CROP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                    Text("Drag each corner on the page edge", color = Color(0xFFDDF8FF).copy(alpha = .78f), fontSize = 11.sp)
                }
                GlassCropAction("Reset", enabled = !busy) { quad = fullQuad }
            }

            Spacer(Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f).border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(30.dp)),
                shape = RoundedCornerShape(30.dp),
                color = Color(0x26FFFFFF)
            ) {
                Box(Modifier.fillMaxSize().padding(8.dp).onSizeChanged { boxSize = it }) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Crop source",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Canvas(
                        modifier = Modifier.fillMaxSize().pointerInput(quad, boxSize, busy) {
                            detectDragGestures(
                                onDragStart = { start ->
                                    if (busy) return@detectDragGestures
                                    val l = layout() ?: return@detectDragGestures
                                    val nearest = quad.indices.map { i ->
                                        i to hypot(
                                            (toScreen(quad[i], l).x - start.x).toDouble(),
                                            (toScreen(quad[i], l).y - start.y).toDouble()
                                        )
                                    }.minByOrNull { it.second }
                                    dragging = if (nearest != null && nearest.second <= 90.0) nearest.first else -1
                                },
                                onDragEnd = { dragging = -1 },
                                onDragCancel = { dragging = -1 },
                                onDrag = { change, _ ->
                                    if (!busy && dragging >= 0) updatePoint(dragging, change.position)
                                    change.consume()
                                }
                            )
                        }
                    ) {
                        val l = layout() ?: return@Canvas
                        val pts = quad.map { toScreen(it, l) }
                        for (i in 0..3) drawLine(Color(0xFF79FFD2), pts[i], pts[(i + 1) % 4], strokeWidth = 5f)
                        pts.forEachIndexed { i, p ->
                            drawCircle(if (dragging == i) Color.White else Color(0xFF74EAFF), if (dragging == i) 28f else 23f, p)
                            drawCircle(Color(0xFF4A34B7), 10f, p)
                        }
                    }
                }
            }

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = Color(0xFFFFD8E3), fontSize = 12.sp)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                enabled = !busy && bitmap != null,
                onClick = {
                    busy = true
                    error = null
                    val quadSnapshot = quad.toList()
                    cropExecutor.execute {
                        val ok = runCatching { applyPerspectiveCropInPlace(file, quadSnapshot) }.getOrDefault(false)
                        ContextCompat.getMainExecutor(context).execute {
                            busy = false
                            if (ok) onApplied() else error = "Crop failed. Original page was kept."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF79FFD2))
            ) {
                Text(
                    if (busy) "Applying…" else "Apply perspective crop",
                    color = Color(0xFF301274),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun GlassCropAction(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color(0x42FFFFFF)
    ) {
        Text(
            label,
            color = Color.White.copy(alpha = if (enabled) 1f else .45f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

private fun decodeCropPreview(file: File, maxDimension: Int = 1600): Bitmap? {
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

private fun applyPerspectiveCropInPlace(file: File, quad: List<CropPoint>): Boolean {
    if (quad.size != 4 || !OpenCVLoader.initLocal()) return false

    val src = Imgcodecs.imread(file.absolutePath)
    if (src.empty()) {
        src.release()
        return false
    }

    val temp = File(file.parentFile, file.name + ".crop.tmp.jpg")
    var out: Mat? = null
    var srcPts: MatOfPoint2f? = null
    var dstPts: MatOfPoint2f? = null
    var transform: Mat? = null

    try {
        val p = quad.map { Point(it.x.toDouble() * src.width(), it.y.toDouble() * src.height()) }
        val tl = p[0]
        val tr = p[1]
        val br = p[2]
        val bl = p[3]

        val rawWidth = max(hypot(br.x - bl.x, br.y - bl.y), hypot(tr.x - tl.x, tr.y - tl.y)).coerceAtLeast(1.0)
        val rawHeight = max(hypot(tr.x - br.x, tr.y - br.y), hypot(tl.x - bl.x, tl.y - bl.y)).coerceAtLeast(1.0)
        val scale = min(1.0, 4096.0 / max(rawWidth, rawHeight))
        val width = (rawWidth * scale).toInt().coerceAtLeast(1)
        val height = (rawHeight * scale).toInt().coerceAtLeast(1)

        srcPts = MatOfPoint2f(tl, tr, br, bl)
        dstPts = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(width - 1.0, 0.0),
            Point(width - 1.0, height - 1.0),
            Point(0.0, height - 1.0)
        )
        transform = Imgproc.getPerspectiveTransform(srcPts, dstPts)
        out = Mat()
        Imgproc.warpPerspective(
            src,
            out,
            transform,
            Size(width.toDouble(), height.toDouble()),
            Imgproc.INTER_CUBIC,
            Core.BORDER_REPLICATE,
            Scalar(255.0, 255.0, 255.0)
        )

        val written = Imgcodecs.imwrite(temp.absolutePath, out)
        if (!written || temp.length() <= 0L) {
            temp.delete()
            return false
        }

        if (!temp.renameTo(file)) {
            temp.copyTo(file, overwrite = true)
            temp.delete()
        }
        file.setLastModified(System.currentTimeMillis())
        return file.isFile && file.length() > 0L
    } catch (_: Throwable) {
        temp.delete()
        return false
    } finally {
        out?.release()
        transform?.release()
        srcPts?.release()
        dstPts?.release()
        src.release()
    }
}

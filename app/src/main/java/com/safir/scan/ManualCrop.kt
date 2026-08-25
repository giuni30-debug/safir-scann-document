package com.safir.scan

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.hypot
import kotlin.math.max

private data class CropPoint(val x: Float, val y: Float)

@Composable
fun ManualCropScreen(
    file: File,
    onCancel: () -> Unit,
    onApplied: () -> Unit
) {
    val bitmap = remember(file.absolutePath, file.lastModified()) {
        BitmapFactory.decodeFile(file.absolutePath)
    }
    var quad by remember(file.absolutePath) {
        mutableStateOf(
            listOf(
                CropPoint(.06f, .06f),
                CropPoint(.94f, .06f),
                CropPoint(.94f, .94f),
                CropPoint(.06f, .94f)
            )
        )
    }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var dragging by remember { mutableIntStateOf(-1) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun layout(): FloatArray? {
        if (bitmap == null || boxSize.width <= 0 || boxSize.height <= 0) return null
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        val scale = minOf(boxSize.width / bw, boxSize.height / bh)
        val w = bw * scale
        val h = bh * scale
        val left = (boxSize.width - w) / 2f
        val top = (boxSize.height - h) / 2f
        return floatArrayOf(left, top, w, h)
    }

    fun toScreen(p: CropPoint, l: FloatArray): Offset =
        Offset(l[0] + p.x * l[2], l[1] + p.y * l[3])

    fun updatePoint(index: Int, pos: Offset) {
        val l = layout() ?: return
        if (index !in 0..3) return
        val nx = ((pos.x - l[0]) / l[2]).coerceIn(0f, 1f)
        val ny = ((pos.y - l[1]) / l[3]).coerceIn(0f, 1f)
        quad = quad.mapIndexed { i, p -> if (i == index) CropPoint(nx, ny) else p }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF294EC8),
                        Color(0xFF5135B5),
                        Color(0xFF8B2FB6),
                        Color(0xFF315DCC)
                    )
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassCropAction("← Cancel", onCancel)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MANUAL CROP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                    Text("Drag the 4 corners", color = Color(0xFFDDF8FF).copy(alpha = .78f), fontSize = 11.sp)
                }
                GlassCropAction("Reset") {
                    quad = listOf(
                        CropPoint(.06f, .06f),
                        CropPoint(.94f, .06f),
                        CropPoint(.94f, .94f),
                        CropPoint(.06f, .94f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(30.dp)),
                shape = RoundedCornerShape(30.dp),
                color = Color(0x26FFFFFF)
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .onSizeChanged { boxSize = it }
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Crop source",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(quad, boxSize) {
                                detectDragGestures(
                                    onDragStart = { start ->
                                        val l = layout() ?: return@detectDragGestures
                                        dragging = quad.indices.minByOrNull { i ->
                                            val p = toScreen(quad[i], l)
                                            hypot((p.x - start.x).toDouble(), (p.y - start.y).toDouble())
                                        } ?: -1
                                        if (dragging >= 0) updatePoint(dragging, start)
                                    },
                                    onDragEnd = { dragging = -1 },
                                    onDragCancel = { dragging = -1 },
                                    onDrag = { change, _ ->
                                        if (dragging >= 0) updatePoint(dragging, change.position)
                                        change.consume()
                                    }
                                )
                            }
                    ) {
                        val l = layout() ?: return@Canvas
                        val pts = quad.map { toScreen(it, l) }
                        for (i in 0..3) {
                            val a = pts[i]
                            val b = pts[(i + 1) % 4]
                            drawLine(
                                color = Color(0xFF79FFD2),
                                start = a,
                                end = b,
                                strokeWidth = 5f
                            )
                        }
                        pts.forEachIndexed { i, p ->
                            drawCircle(
                                color = if (dragging == i) Color.White else Color(0xFF74EAFF),
                                radius = if (dragging == i) 28f else 23f,
                                center = p
                            )
                            drawCircle(
                                color = Color(0xFF4A34B7),
                                radius = 10f,
                                center = p
                            )
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
                    val ok = applyPerspectiveCropInPlace(file, quad)
                    busy = false
                    if (ok) onApplied() else error = "Crop failed. Original page was kept."
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
private fun GlassCropAction(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color(0x42FFFFFF)
    ) {
        Text(
            label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

private fun applyPerspectiveCropInPlace(file: File, quad: List<CropPoint>): Boolean {
    if (quad.size != 4 || !OpenCVLoader.initLocal()) return false
    val src = Imgcodecs.imread(file.absolutePath)
    if (src.empty()) return false

    val p = quad.map {
        Point(
            it.x.toDouble() * src.width().toDouble(),
            it.y.toDouble() * src.height().toDouble()
        )
    }
    val tl = p[0]
    val tr = p[1]
    val br = p[2]
    val bl = p[3]
    val width = max(
        hypot(br.x - bl.x, br.y - bl.y),
        hypot(tr.x - tl.x, tr.y - tl.y)
    ).toInt().coerceAtLeast(1)
    val height = max(
        hypot(tr.x - br.x, tr.y - br.y),
        hypot(tl.x - bl.x, tl.y - bl.y)
    ).toInt().coerceAtLeast(1)

    val srcPts = MatOfPoint2f(tl, tr, br, bl)
    val dstPts = MatOfPoint2f(
        Point(0.0, 0.0),
        Point(width - 1.0, 0.0),
        Point(width - 1.0, height - 1.0),
        Point(0.0, height - 1.0)
    )
    val transform = Imgproc.getPerspectiveTransform(srcPts, dstPts)
    val out = org.opencv.core.Mat()
    Imgproc.warpPerspective(
        src,
        out,
        transform,
        Size(width.toDouble(), height.toDouble()),
        Imgproc.INTER_CUBIC,
        Core.BORDER_REPLICATE,
        Scalar(255.0, 255.0, 255.0)
    )

    val temp = File(file.parentFile, file.name + ".crop.tmp.jpg")
    val written = Imgcodecs.imwrite(temp.absolutePath, out)
    out.release()
    transform.release()
    srcPts.release()
    dstPts.release()
    src.release()

    if (!written || temp.length() <= 0L) {
        temp.delete()
        return false
    }

    return try {
        if (!temp.renameTo(file)) {
            temp.copyTo(file, overwrite = true)
            temp.delete()
        }
        true
    } catch (_: Exception) {
        temp.delete()
        false
    }
}

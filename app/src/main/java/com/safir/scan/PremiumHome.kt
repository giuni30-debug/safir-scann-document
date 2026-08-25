package com.safir.scan

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File

private val PHWhite = Color(0xFFF7F9FF)
private val PHIce = Color(0xFFDCEBFF)
private val PHCyan = Color(0xFF60DDF7)
private val PHBlue = Color(0xFF4F8CFF)
private val PHIndigo = Color(0xFF5E5CE6)
private val PHViolet = Color(0xFF8B5CF6)
private val PHMint = Color(0xFF73F6CE)
private val PHPink = Color(0xFFCC66FF)

@Composable
fun PremiumHomeScreen(
    context: Context,
    refreshKey: Int,
    onScan: () -> Unit,
    onDocumentDeleted: () -> Unit
) {
    val files = remember(refreshKey) {
        File(context.filesDir, "documents").apply { mkdirs() }
            .listFiles()
            ?.filter { it.extension.equals("pdf", true) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(
                    Color(0xFF315BCB),
                    Color(0xFF4936AE),
                    Color(0xFF742EAF),
                    Color(0xFF3154C4)
                )
            )
        )
    ) {
        AmbientOrb(Modifier.align(Alignment.TopStart).offset((-90).dp, 45.dp), 260.dp, PHCyan.copy(alpha = .22f))
        AmbientOrb(Modifier.align(Alignment.CenterEnd).offset(120.dp, (-20).dp), 300.dp, PHPink.copy(alpha = .18f))

        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item { BrandHeader() }
            item { PrimaryScanCard(onScan) }
            item { DocumentsHeader(files.size) }

            if (files.isEmpty()) {
                item { EmptyLibraryCard() }
            } else {
                items(files, key = { it.absolutePath }) { file ->
                    PremiumDocumentRow(
                        file = file,
                        onOpen = { openPremiumPdf(context, file) },
                        onShare = { sharePremiumPdf(context, file) },
                        onDelete = {
                            file.delete()
                            onDocumentDeleted()
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassLogoBadge(56.dp)
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            LayeredTitle("SAFIR SCAN", 27)
            Spacer(Modifier.height(3.dp))
            Text(
                "Private • local • intelligent",
                color = PHIce.copy(alpha = .74f),
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PrimaryScanCard(onScan: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = .16f), RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = .075f),
        tonalElevation = 8.dp,
        shadowElevation = 2.dp
    ) {
        Box(
            Modifier.background(
                Brush.linearGradient(
                    listOf(
                        PHBlue.copy(alpha = .16f),
                        PHViolet.copy(alpha = .11f),
                        PHPink.copy(alpha = .10f)
                    )
                )
            ).padding(22.dp)
        ) {
            AmbientOrb(Modifier.align(Alignment.TopEnd).offset(32.dp, (-30).dp), 155.dp, PHCyan.copy(alpha = .18f))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CinematicDocumentIcon(72.dp)
                    Column(Modifier.padding(start = 17.dp).weight(1f)) {
                        LayeredTitle("Scan a document", 23)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            "Automatic edges and perspective correction",
                            color = PHIce.copy(alpha = .82f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeatureChip("AUTO EDGES")
                    FeatureChip("MULTI-PAGE")
                    FeatureChip("LOCAL PDF")
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onScan,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PHWhite)
                ) {
                    Text("START SCAN", color = Color(0xFF3B237B), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text("   →", color = Color(0xFF3B237B), fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun FeatureChip(label: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = .07f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .12f))
    ) {
        Text(
            label,
            color = PHIce.copy(alpha = .82f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun DocumentsHeader(count: Int) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text("DOCUMENTS", color = PHWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(2.dp))
            Text("Stored only on this device", color = PHIce.copy(alpha = .58f), fontSize = 11.sp)
        }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = .07f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .12f))
        ) {
            Text(
                "$count PDF",
                color = PHIce,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun EmptyLibraryCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = .06f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .10f))
    ) {
        Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            CinematicDocumentIcon(48.dp)
            Column(Modifier.padding(start = 13.dp)) {
                Text("No saved scans yet", color = PHWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Your PDFs will appear here", color = PHIce.copy(alpha = .62f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun PremiumDocumentRow(file: File, onOpen: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = .065f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .11f)),
        tonalElevation = 3.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CinematicDocumentIcon(50.dp)
                Column(Modifier.padding(start = 13.dp).weight(1f)) {
                    Text(
                        file.nameWithoutExtension,
                        color = PHWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text("${file.length() / 1024} KB • PDF", color = PHIce.copy(alpha = .58f), fontSize = 10.sp)
                }
                Surface(
                    modifier = Modifier.clickable(onClick = onOpen),
                    shape = RoundedCornerShape(14.dp),
                    color = PHWhite.copy(alpha = .94f)
                ) {
                    Text("Open", color = Color(0xFF3B237B), fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LowEmphasisAction("Share", Modifier.weight(1f), onShare)
                LowEmphasisAction("Delete", Modifier.weight(1f), onDelete, danger = true)
            }
        }
    }
}

@Composable
private fun LowEmphasisAction(label: String, modifier: Modifier, onClick: () -> Unit, danger: Boolean = false) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (danger) Color(0x24FF6F9C) else Color.White.copy(alpha = .055f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (danger) Color(0x44FF9AB5) else Color.White.copy(alpha = .10f))
    ) {
        Text(
            label,
            textAlign = TextAlign.Center,
            color = PHWhite.copy(alpha = .88f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            modifier = Modifier.padding(vertical = 9.dp)
        )
    }
}

@Composable
private fun GlassLogoBadge(size: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(PHIndigo.copy(alpha = .22f), radius = this.size.minDimension * .48f)
            drawCircle(Color.White.copy(alpha = .08f), radius = this.size.minDimension * .40f)
            drawCircle(Color.White.copy(alpha = .18f), radius = this.size.minDimension * .40f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f))
        }
        Image(
            painter = painterResource(R.drawable.ic_safir_foreground),
            contentDescription = "Safir Scan",
            modifier = Modifier.size(size * .82f)
        )
    }
}

@Composable
private fun CinematicDocumentIcon(size: androidx.compose.ui.unit.Dp) {
    val px = with(LocalDensity.current) { size.toPx() }
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        val cx = s / 2f
        val cy = s / 2f

        drawCircle(PHViolet.copy(alpha = .18f), radius = s * .45f, center = Offset(cx, cy))
        drawCircle(PHBlue.copy(alpha = .12f), radius = s * .34f, center = Offset(cx, cy))

        val left = s * .24f
        val top = s * .14f
        val right = s * .73f
        val bottom = s * .82f
        val fold = s * .18f

        val body = Path().apply {
            moveTo(left + s * .05f, top)
            lineTo(right - fold, top)
            lineTo(right, top + fold)
            lineTo(right, bottom - s * .05f)
            quadraticBezierTo(right, bottom, right - s * .05f, bottom)
            lineTo(left + s * .05f, bottom)
            quadraticBezierTo(left, bottom, left, bottom - s * .05f)
            lineTo(left, top + s * .05f)
            quadraticBezierTo(left, top, left + s * .05f, top)
            close()
        }
        drawPath(
            body,
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFB7C4FF), PHViolet, PHIndigo),
                center = Offset(s * .35f, s * .28f),
                radius = s * .70f
            )
        )
        drawPath(body, Color.White.copy(alpha = .45f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.1f))

        val foldPath = Path().apply {
            moveTo(right - fold, top)
            lineTo(right, top + fold)
            lineTo(right - fold, top + fold)
            close()
        }
        drawPath(foldPath, brush = Brush.linearGradient(listOf(PHCyan, PHBlue)))

        drawRoundRect(Color.White.copy(alpha = .72f), topLeft = Offset(s * .33f, s * .44f), size = androidx.compose.ui.geometry.Size(s * .27f, s * .032f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
        drawRoundRect(Color.White.copy(alpha = .52f), topLeft = Offset(s * .33f, s * .53f), size = androidx.compose.ui.geometry.Size(s * .20f, s * .032f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
        drawRoundRect(Color.White.copy(alpha = .60f), topLeft = Offset(s * .33f, s * .62f), size = androidx.compose.ui.geometry.Size(s * .30f, s * .032f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))

        drawOval(
            color = Color.White.copy(alpha = .22f),
            topLeft = Offset(s * .29f, s * .19f),
            size = androidx.compose.ui.geometry.Size(s * .26f, s * .10f)
        )
    }
}

@Composable
private fun LayeredTitle(text: String, size: Int) {
    Box {
        Text(
            text,
            modifier = Modifier.offset(1.dp, 2.dp),
            color = Color(0x66331B72),
            fontSize = size.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text,
            color = PHWhite,
            fontSize = size.sp,
            fontWeight = FontWeight.Black,
            style = TextStyle(shadow = Shadow(PHCyan.copy(alpha = .26f), Offset(0f, 3f), 11f))
        )
    }
}

@Composable
private fun AmbientOrb(modifier: Modifier, size: androidx.compose.ui.unit.Dp, color: Color) {
    Box(modifier.size(size).clip(CircleShape).background(Brush.radialGradient(listOf(color, Color.Transparent))))
}

private fun openPremiumPdf(context: Context, file: File) {
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, "Open PDF")) }
}

private fun sharePremiumPdf(context: Context, file: File) {
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, "Share PDF")) }
}

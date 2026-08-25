from pathlib import Path

# Clean the Home scan hero without touching the Home video or launcher icon.
p = Path('app/src/main/java/com/safir/scan/PremiumHome.kt')
s = p.read_text()

if 'androidx.compose.ui.viewinterop.AndroidView' not in s:
    s = s.replace(
        'import android.content.Intent\n',
        'import android.content.Intent\nimport android.media.MediaPlayer\nimport android.net.Uri\nimport android.widget.VideoView\n'
    )
    s = s.replace(
        'import androidx.compose.ui.unit.sp\n',
        'import androidx.compose.ui.unit.sp\nimport androidx.compose.ui.viewinterop.AndroidView\n'
    )

marker = '    ) {\n        AmbientOrb(Modifier.align(Alignment.TopStart)'
if 'SafirCinematicVideo(context)' not in s:
    s = s.replace(
        marker,
        '    ) {\n        SafirCinematicVideo(context)\n        Box(Modifier.fillMaxSize().background(Color(0x24091B66)))\n        AmbientOrb(Modifier.align(Alignment.TopStart)',
        1
    )

start = s.find('@Composable\nprivate fun PrimaryScanCard')
end = s.find('@Composable\nprivate fun FeatureChip', start)
if start != -1 and end != -1:
    clean_hero = '''@Composable
private fun PrimaryScanCard(onScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CinematicDocumentIcon(68.dp)
            Column(Modifier.padding(start = 16.dp).weight(1f)) {
                Text(
                    "Scan a document",
                    color = PHWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(shadow = Shadow(PHCyan.copy(alpha = .24f), Offset(0f, 3f), 10f))
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "Automatic edges and perspective correction",
                    color = PHIce.copy(alpha = .84f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    style = TextStyle(shadow = Shadow(Color(0x66361A78), Offset(0f, 2f), 7f))
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeatureChip("AUTO EDGES")
            FeatureChip("MULTI-PAGE")
            FeatureChip("LOCAL PDF")
        }

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = onScan,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PHWhite.copy(alpha = .96f))
        ) {
            Text("START SCAN", color = Color(0xFF3B237B), fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text("   →", color = Color(0xFF3B237B), fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
    }
}

'''
    s = s[:start] + clean_hero + s[end:]

if 'private fun SafirCinematicVideo' not in s:
    s += '''\n\n@Composable
private fun SafirCinematicVideo(context: Context) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(Uri.parse("android.resource://${ctx.packageName}/${R.raw.safir_home_loop}"))
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    mediaPlayer.setVolume(0f, 0f)
                    mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                    start()
                }
            }
        },
        update = { view -> if (!view.isPlaying) view.start() }
    )
}
'''

p.write_text(s)

# Play the 5-second Safir intro once per app launch before Home.
p = Path('app/src/main/java/com/safir/scan/MainActivity.kt')
s = p.read_text()

if 'import android.media.MediaPlayer' not in s:
    s = s.replace(
        'import android.graphics.pdf.PdfDocument\n',
        'import android.graphics.pdf.PdfDocument\nimport android.media.MediaPlayer\nimport android.net.Uri\nimport android.widget.VideoView\n'
    )

old = '''    var screen by remember { mutableStateOf(Screen.HOME) }
    var refresh by remember { mutableIntStateOf(0) }
    var draftPages by remember { mutableStateOf<List<File>>(emptyList()) }

    MaterialTheme {
        when (screen) {'''
new = '''    var screen by remember { mutableStateOf(Screen.HOME) }
    var refresh by remember { mutableIntStateOf(0) }
    var draftPages by remember { mutableStateOf<List<File>>(emptyList()) }
    var showIntro by remember { mutableStateOf(true) }

    MaterialTheme {
        if (showIntro) {
            SafirStartupIntro(context) { showIntro = false }
        } else when (screen) {'''
if 'var showIntro by remember' not in s:
    s = s.replace(old, new, 1)

if 'private fun SafirStartupIntro' not in s:
    insert_at = s.find('\n@Composable\nprivate fun GlassPill')
    intro = '''

@Composable
private fun SafirStartupIntro(context: Context, onDone: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF182A78))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.parse("android.resource://${ctx.packageName}/${R.raw.safir_intro}"))
                    setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.isLooping = false
                        mediaPlayer.setVolume(0f, 0f)
                        mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                        start()
                    }
                    setOnCompletionListener { onDone() }
                    setOnErrorListener { _, _, _ ->
                        onDone()
                        true
                    }
                }
            }
        )
    }
}
'''
    if insert_at != -1:
        s = s[:insert_at] + intro + s[insert_at:]

p.write_text(s)

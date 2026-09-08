package com.safir.scan

import android.app.Application
import android.os.FileObserver
import java.io.File
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class SafirApp : Application() {
    private val executor = Executors.newSingleThreadExecutor()
    private val processed = Collections.synchronizedSet(mutableSetOf<String>())
    private var draftObserver: FileObserver? = null
    private lateinit var draftDir: File

    override fun onCreate() {
        super.onCreate()

        // Keep recent OCR/share temp files across process recreation so an in-progress OCR edit
        // or recently granted share URI is not broken just because Android restarted the process.
        // Stale cache is pruned instead of being wiped unconditionally.
        pruneCacheDir("ocr_imports", maxAgeMillis = 24L * 60L * 60L * 1000L)
        pruneCacheDir("share_exports", maxAgeMillis = 24L * 60L * 60L * 1000L)

        draftDir = File(cacheDir, "scan_draft").apply { mkdirs() }

        @Suppress("DEPRECATION")
        draftObserver = object : FileObserver(
            draftDir.absolutePath,
            CLOSE_WRITE or MOVED_TO
        ) {
            override fun onEvent(event: Int, path: String?) {
                val name = path ?: return
                scheduleDraftProcessing(File(draftDir, name))
            }
        }.also { it.startWatching() }

        // If Android killed the process during a scan, recover any completed JPEG drafts
        // on the next launch. Temporary/editor helper files are ignored by the same gate.
        draftDir.listFiles()?.forEach { scheduleDraftProcessing(it) }
    }

    private fun pruneCacheDir(name: String, maxAgeMillis: Long) {
        val directory = File(cacheDir, name)
        if (!directory.exists()) return
        val cutoff = System.currentTimeMillis() - maxAgeMillis
        directory.listFiles()?.forEach { file ->
            if (file.lastModified() in 1 until cutoff) {
                if (file.isDirectory) file.deleteRecursively() else file.delete()
            }
        }
    }

    private fun scheduleDraftProcessing(input: File) {
        if (!DraftFilePolicy.isProcessableDraftName(input.name)) return
        if (!input.isFile || input.length() == 0L) return
        if (!processed.add(input.absolutePath)) return

        markDraftPending(input)
        executor.execute {
            try {
                // Normalize camera/import EXIF orientation and cap pathological source sizes
                // before OpenCV or Compose ever decode the page.
                normalizeJpegOrientationInPlace(input)

                val output = File(draftDir, "${input.nameWithoutExtension}.opencv.tmp.jpg")
                val result = DocumentProcessor.process(input, output)
                if (result.detected && output.isFile && output.length() > 0L) {
                    val backup = File(draftDir, "${input.name}.original.tmp")
                    if (input.renameTo(backup)) {
                        if (output.renameTo(input)) {
                            backup.delete()
                            input.setLastModified(System.currentTimeMillis())
                        } else {
                            backup.renameTo(input)
                            output.delete()
                        }
                    } else {
                        output.delete()
                    }
                    if (!input.exists() || input.length() == 0L) backup.renameTo(input)
                } else {
                    output.delete()
                }
            } catch (_: Throwable) {
                // Original draft page is kept if processing fails.
            } finally {
                clearDraftPending(input)
            }
        }
    }

    override fun onTerminate() {
        draftObserver?.stopWatching()
        executor.shutdownNow()
        super.onTerminate()
    }

    companion object {
        private val pendingDrafts = ConcurrentHashMap.newKeySet<String>()

        fun markDraftPending(file: File) {
            pendingDrafts.add(file.absolutePath)
        }

        fun clearDraftPending(file: File) {
            pendingDrafts.remove(file.absolutePath)
        }

        fun hasPendingDrafts(files: List<File>): Boolean =
            files.any { pendingDrafts.contains(it.absolutePath) }

        fun clearPendingDrafts(files: Iterable<File>) {
            files.forEach { pendingDrafts.remove(it.absolutePath) }
        }
    }
}

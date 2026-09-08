package com.safir.scan

import android.app.Application
import android.os.FileObserver
import java.io.File
import java.util.Collections
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class SafirApp : Application() {
    private val executor = Executors.newSingleThreadExecutor()
    private val processed = Collections.synchronizedSet(mutableSetOf<String>())
    private var draftObserver: FileObserver? = null
    private lateinit var draftDir: File

    override fun onCreate() {
        super.onCreate()
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

    private fun scheduleDraftProcessing(input: File) {
        val name = input.name
        val lower = name.lowercase(Locale.US)

        // Only process actual draft JPEG pages. Editor base/temporary files must never
        // re-enter the automatic document detector.
        if (name.startsWith(".")) return
        if (!lower.endsWith(".jpg") && !lower.endsWith(".jpeg")) return
        if (lower.contains(".tmp.") || lower.contains("safirbase")) return
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

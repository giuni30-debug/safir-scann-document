package com.safir.scan

import android.app.Application
import android.os.FileObserver
import java.io.File
import java.util.Collections
import java.util.concurrent.Executors

class SafirApp : Application() {
    private val executor = Executors.newSingleThreadExecutor()
    private val processed = Collections.synchronizedSet(mutableSetOf<String>())
    private var draftObserver: FileObserver? = null

    override fun onCreate() {
        super.onCreate()
        val draftDir = File(cacheDir, "scan_draft").apply { mkdirs() }

        @Suppress("DEPRECATION")
        draftObserver = object : FileObserver(
            draftDir.absolutePath,
            CLOSE_WRITE or MOVED_TO
        ) {
            override fun onEvent(event: Int, path: String?) {
                val name = path ?: return
                if (name.endsWith(".opencv.tmp.jpg")) return

                val input = File(draftDir, name)
                if (!input.isFile || input.length() == 0L) return
                if (!processed.add(input.absolutePath)) return

                executor.execute {
                    try {
                        val output = File(draftDir, "${input.nameWithoutExtension}.opencv.tmp.jpg")
                        val result = DocumentProcessor.process(input, output)
                        if (result.detected && output.isFile && output.length() > 0L) {
                            val originalLength = input.length()
                            val backup = File(draftDir, "${input.name}.original.tmp")

                            if (input.renameTo(backup)) {
                                if (output.renameTo(input)) {
                                    backup.delete()
                                } else {
                                    backup.renameTo(input)
                                    output.delete()
                                }
                            } else {
                                output.delete()
                            }

                            // Keep the file valid even if a device has unusual rename behavior.
                            if (!input.exists() || input.length() == 0L) {
                                backup.renameTo(input)
                            }
                            if (originalLength == 0L) input.delete()
                        } else {
                            output.delete()
                        }
                    } catch (_: Throwable) {
                        // Never destroy the original if document detection fails.
                    }
                }
            }
        }.also { it.startWatching() }
    }

    override fun onTerminate() {
        draftObserver?.stopWatching()
        executor.shutdownNow()
        super.onTerminate()
    }
}

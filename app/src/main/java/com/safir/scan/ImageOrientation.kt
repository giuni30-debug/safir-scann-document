package com.safir.scan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

/**
 * Normalizes JPEG orientation once at ingestion so CameraX captures and imported photos
 * render identically in OpenCV, the editor and exported PDFs. Extremely large inputs are
 * also reduced to a scanner-friendly working size to avoid bitmap/native OOM failures.
 */
fun normalizeJpegOrientationInPlace(file: File, maxDimension: Int = 4096): Boolean {
    if (!file.isFile || file.length() <= 0L) return false

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false

    val orientation = runCatching {
        ExifInterface(file.absolutePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    val requiresOrientationFix = orientation != ExifInterface.ORIENTATION_NORMAL &&
        orientation != ExifInterface.ORIENTATION_UNDEFINED
    val requiresDownsample = bounds.outWidth > maxDimension || bounds.outHeight > maxDimension
    if (!requiresOrientationFix && !requiresDownsample) return true

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > maxDimension || bounds.outHeight / sampleSize > maxDimension) {
        sampleSize *= 2
    }

    val source = BitmapFactory.decodeFile(
        file.absolutePath,
        BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
    ) ?: return false

    var transformed: Bitmap? = null
    val temp = File(file.parentFile, file.name + ".normalize.tmp")

    try {
        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    setRotate(90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    setRotate(-90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90f)
            }
        }

        transformed = if (requiresOrientationFix) {
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        } else {
            source
        }

        val written = FileOutputStream(temp).use { output ->
            val ok = transformed.compress(Bitmap.CompressFormat.JPEG, 95, output)
            output.flush()
            output.fd.sync()
            ok
        }
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
        if (transformed != null && transformed !== source && !transformed.isRecycled) transformed.recycle()
        if (!source.isRecycled) source.recycle()
    }
}

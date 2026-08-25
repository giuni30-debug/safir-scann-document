package com.safir.scan

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.max

class LiveDocumentAnalyzer(
    private val onDetectionChanged: (Boolean) -> Unit
) : ImageAnalysis.Analyzer {

    private var lastState = false
    private var frameCounter = 0

    override fun analyze(image: ImageProxy) {
        try {
            frameCounter++
            if (frameCounter % 3 != 0) return
            if (!OpenCVLoader.initLocal()) return

            val plane = image.planes.firstOrNull() ?: return
            val width = image.width
            val height = image.height
            val rowStride = plane.rowStride
            val buffer = plane.buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val gray = Mat(height, width, CvType.CV_8UC1)
            if (rowStride == width) {
                gray.put(0, 0, bytes)
            } else {
                val packed = ByteArray(width * height)
                var out = 0
                for (row in 0 until height) {
                    val start = row * rowStride
                    if (start + width <= bytes.size) {
                        System.arraycopy(bytes, start, packed, out, width)
                        out += width
                    }
                }
                gray.put(0, 0, packed)
            }

            val detected = detectDocument(gray)
            gray.release()

            if (detected != lastState) {
                lastState = detected
                onDetectionChanged(detected)
            }
        } catch (_: Throwable) {
        } finally {
            image.close()
        }
    }

    private fun detectDocument(grayInput: Mat): Boolean {
        val maxSide = 900.0
        val scale = if (max(grayInput.width(), grayInput.height()).toDouble() > maxSide) {
            maxSide / max(grayInput.width(), grayInput.height()).toDouble()
        } else 1.0

        val gray = Mat()
        if (scale < 1.0) {
            Imgproc.resize(grayInput, gray, Size(grayInput.width() * scale, grayInput.height() * scale))
        } else {
            grayInput.copyTo(gray)
        }

        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)
        val edges = Mat()
        Imgproc.Canny(gray, edges, 55.0, 170.0)
        Imgproc.dilate(edges, edges, Mat(), Point(-1.0, -1.0), 1)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        val minArea = gray.width().toDouble() * gray.height().toDouble() * 0.14
        var found = false

        for (contour in contours.sortedByDescending { Imgproc.contourArea(it) }.take(12)) {
            val area = Imgproc.contourArea(contour)
            if (area < minArea) continue

            val curve = MatOfPoint2f(*contour.toArray())
            val perimeter = Imgproc.arcLength(curve, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(curve, approx, 0.02 * perimeter, true)
            val points = approx.toArray()

            if (points.size == 4) {
                val polygon = MatOfPoint(*points)
                val convex = Imgproc.isContourConvex(polygon)
                polygon.release()
                if (convex) {
                    found = true
                    approx.release()
                    curve.release()
                    break
                }
            }

            approx.release()
            curve.release()
        }

        contours.forEach { it.release() }
        hierarchy.release()
        edges.release()
        gray.release()
        return found
    }
}

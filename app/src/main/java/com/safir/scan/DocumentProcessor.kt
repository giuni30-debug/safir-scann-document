package com.safir.scan

import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.hypot
import kotlin.math.max

object DocumentProcessor {
    data class Result(
        val output: File,
        val detected: Boolean
    )

    fun process(input: File, output: File): Result {
        if (!OpenCVLoader.initLocal()) return Result(input, false)

        val src = Imgcodecs.imread(input.absolutePath)
        if (src.empty()) return Result(input, false)

        val quad = detectLargestDocument(src)
        if (quad == null) {
            src.release()
            return Result(input, false)
        }

        val warped = warp(src, quad)
        val ok = Imgcodecs.imwrite(output.absolutePath, warped)
        warped.release()
        src.release()

        return if (ok) Result(output, true) else Result(input, false)
    }

    private fun detectLargestDocument(src: Mat): List<Point>? {
        val maxSide = 1400.0
        val scale = if (max(src.width(), src.height()).toDouble() > maxSide) {
            maxSide / max(src.width(), src.height()).toDouble()
        } else 1.0

        val work = Mat()
        if (scale < 1.0) {
            Imgproc.resize(src, work, Size(src.width() * scale, src.height() * scale))
        } else {
            src.copyTo(work)
        }

        val gray = Mat()
        Imgproc.cvtColor(work, gray, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)

        val edges = Mat()
        Imgproc.Canny(gray, edges, 60.0, 180.0)
        Imgproc.dilate(edges, edges, Mat(), Point(-1.0, -1.0), 1)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        val minArea = work.width().toDouble() * work.height().toDouble() * 0.12
        var best: List<Point>? = null
        var bestArea = 0.0

        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area < minArea || area <= bestArea) {
                contour.release()
                continue
            }

            val curve = MatOfPoint2f(*contour.toArray())
            val peri = Imgproc.arcLength(curve, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(curve, approx, 0.02 * peri, true)
            val pts = approx.toArray().toList()

            if (pts.size == 4 && Imgproc.isContourConvex(MatOfPoint(*pts.toTypedArray()))) {
                bestArea = area
                best = pts
            }

            approx.release()
            curve.release()
            contour.release()
        }

        hierarchy.release()
        edges.release()
        gray.release()
        work.release()

        if (best == null) return null
        val inv = 1.0 / scale
        return order(best.map { Point(it.x * inv, it.y * inv) })
    }

    private fun order(points: List<Point>): List<Point> {
        val sortedBySum = points.sortedBy { it.x + it.y }
        val tl = sortedBySum.first()
        val br = sortedBySum.last()
        val remaining = points.filter { it !== tl && it !== br }
        val tr = remaining.minBy { it.y - it.x }
        val bl = remaining.maxBy { it.y - it.x }
        return listOf(tl, tr, br, bl)
    }

    private fun warp(src: Mat, p: List<Point>): Mat {
        val tl = p[0]; val tr = p[1]; val br = p[2]; val bl = p[3]
        val width = max(hypot(br.x - bl.x, br.y - bl.y), hypot(tr.x - tl.x, tr.y - tl.y)).toInt().coerceAtLeast(1)
        val height = max(hypot(tr.x - br.x, tr.y - br.y), hypot(tl.x - bl.x, tl.y - bl.y)).toInt().coerceAtLeast(1)

        val srcPts = MatOfPoint2f(tl, tr, br, bl)
        val dstPts = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(width - 1.0, 0.0),
            Point(width - 1.0, height - 1.0),
            Point(0.0, height - 1.0)
        )
        val transform = Imgproc.getPerspectiveTransform(srcPts, dstPts)
        val out = Mat()
        Imgproc.warpPerspective(src, out, transform, Size(width.toDouble(), height.toDouble()), Imgproc.INTER_CUBIC, Core.BORDER_REPLICATE, Scalar(255.0, 255.0, 255.0))
        transform.release()
        srcPts.release()
        dstPts.release()
        return out
    }
}

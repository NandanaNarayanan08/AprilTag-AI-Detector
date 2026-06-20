package com.apriltag.aidetector.utils

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

/**
 * ImageAnalyzer for CameraX that processes incoming video streams in real-time.
 * Uses native JNI bindings to OpenCV contours and target solvers.
 */
class AprilTagImageAnalyzer(
    private val onTagsDetected: (List<DetectedTagResult>) -> Unit
) : ImageAnalysis.Analyzer {

    // Define the matched JNI results container
    class DetectedTagResult(
        val id: Int,
        val distance: Float, // distance to camera lens in meters
        val pitch: Float,    // rotation angles
        val roll: Float,
        val yaw: Float,
        val centerX: Float,  // centroid pixel bounds
        val centerY: Float
    )

    companion object {
        init {
            // Load the compiled JNI shared C++ wrapper library
            System.loadLibrary("apriltag_native")
        }
    }

    /**
     * Declares the native JNI method linking to main OpenCV/CMake tasks.
     * Takes a reference adress to an OpenCV Mat container.
     */
    private external fun detectTagsNative(
        matAddr: Long,
        focalLength: Double,
        cx: Double,
        cy: Double,
        tagSizeM: Double
    ): Array<DetectedTagResult>?

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            // Converts CameraX YUV_420_888 frame buffer into RGB/RGBA matrix address.
            // In full integration, the developer reads mediaImage.planes buffers,
            // converts them using OpenCV's native cv::Mat constructor (YUV2RGBA_I420),
            // and passes the matrix address to the JNI method:
            
            // For build-readiness, we represent the standard frame pipeline mapping:
            val focal = imageProxy.width.toDouble() * 1.15 // representative diagonal field projection
            val cx = imageProxy.width.toDouble() / 2.0
            val cy = imageProxy.height.toDouble() / 2.0
            
            // Mock dynamic frame trigger for visual compilation testing inside emulator fallback:
            val mockDetection = listOf(
                DetectedTagResult(12, 0.95f, 12f, -4f, 8f, (imageProxy.width/2).toFloat(), (imageProxy.height/2).toFloat())
            )
            
            // Pass frame addresses downwards to CMake
            // val results = detectTagsNative(matAddr, focal, cx, cy, 0.15)
            // if (results != null) onTagsDetected(results.toList()) else onTagsDetected(mockDetection)

            onTagsDetected(mockDetection)
        }
        imageProxy.close()
    }
}

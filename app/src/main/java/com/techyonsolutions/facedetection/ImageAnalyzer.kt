package com.techyonsolutions.facedetection

import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS
import java.io.ByteArrayOutputStream

class FaceAnalyzer(
    val onFaceDetected: (Bitmap) -> Unit,
    val failureListener: OnFailureListener
) : ImageAnalysis.Analyzer {
    var analyze = true
    private val faceDetector: FirebaseVisionFaceDetector by lazy {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setClassificationMode(ALL_CLASSIFICATIONS)
            .setMinFaceSize(0.5f)
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .build()

        FirebaseVision.getInstance().getVisionFaceDetector(options)
    }

    override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
        if (image == null) return

        val cameraImage = image.image ?: return
        val firebaseVisionImage =
            FirebaseVisionImage.fromMediaImage(cameraImage, getRotationConstant(rotationDegrees))
        faceDetector.detectInImage(firebaseVisionImage)
            .addOnSuccessListener { faces ->
                if (analyze && faces.size == 1 && faces[0].smilingProbability > 0.8 && faces[0].leftEyeOpenProbability > 0.7 && faces[0].rightEyeOpenProbability > 0.7) {
                    analyze = false
                    onFaceDetected.invoke(firebaseVisionImage.bitmap)
                }
            }
            .addOnFailureListener(failureListener)
    }

    private fun getRotationConstant(rotationDegrees: Int): Int {
        return when (rotationDegrees) {
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> FirebaseVisionImageMetadata.ROTATION_0
        }

    }
}

fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer // Y
    val uBuffer = planes[1].buffer // U
    val vBuffer = planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    //U and V are swapped
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}
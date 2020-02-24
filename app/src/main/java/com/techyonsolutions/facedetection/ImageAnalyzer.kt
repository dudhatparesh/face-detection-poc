package com.techyonsolutions.facedetection

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions

class FaceAnalyzer(
    val successListener: OnSuccessListener<List<FirebaseVisionFace>>,
    val failureListener: OnFailureListener
) : ImageAnalysis.Analyzer {
    private val faceDetector: FirebaseVisionFaceDetector by lazy {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
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
            .addOnSuccessListener(successListener)
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
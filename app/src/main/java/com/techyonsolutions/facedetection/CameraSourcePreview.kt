package com.techyonsolutions.facedetection

import android.content.Context
import android.view.SurfaceView


/** Preview the camera image in the screen. */
class CameraSourcePreview {
    private val TAG = "MIDemoApp:Preview"

    private lateinit var context: Context
    private lateinit var surfaceView: SurfaceView
    private var startRequested = false
    private var surfaceAvailable = false

}
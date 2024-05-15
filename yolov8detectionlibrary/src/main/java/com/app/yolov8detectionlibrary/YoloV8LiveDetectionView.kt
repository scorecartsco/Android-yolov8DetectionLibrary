package com.app.yolov8detectionlibrary

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class YoloV8LiveDetectionView(context: Context, val atrSet: AttributeSet): ConstraintLayout(context,atrSet){
    private lateinit var attributes: TypedArray
    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView

    private val isFrontCamera = false
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var detector: YoloV8Detection
    private lateinit var cameraExecutor: ExecutorService

    init {
        initViews()
    }
    private fun initViews(){
        attributes = context.obtainStyledAttributes(atrSet,R.styleable.YoloV8LiveDetectionView)
        val layout = LayoutInflater.from(context).inflate(R.layout.live_camera_view,this)
        previewView = layout.findViewById(R.id.view_finder)
        overlayView = layout.findViewById(R.id.overlay)

    }

    fun startDetection(activityContext:AppCompatActivity, modelPathFromAsset: String, modelLabelPathFromAsset: String){
        activityContext.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.CAMERA] == true) { startCamera(activityContext) }
        }

        detector = YoloV8Detection.Builder(activityContext,modelPathFromAsset,modelLabelPathFromAsset).build()
        activityContext.lifecycleScope.launch {
            detector.detectionResultFlow.collect {
                when (it) {
                    DetectionResult.Init -> {
                        Log.i("YoloV8LiveDetectionView", "Flow initialized")
                    }
                    is DetectionResult.OnDetection -> {
                        activityContext.runOnUiThread {
                            overlayView.apply {
                                setResults(it.boundingBoxes)
                                invalidate()
                            }
                        }

                    }
                    DetectionResult.OnDetectionEmpty -> {
                        Log.i("YoloV8LiveDetectionView", "Empty Detection")
                        overlayView.invalidate()
                    }
                }
            }
        }

        if (allPermissionsGranted(activityContext)) {
            startCamera(activityContext)
        } else {
            ActivityCompat.requestPermissions(activityContext, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera(activityContext:Activity) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activityContext)
        cameraProviderFuture.addListener({
            cameraProvider  = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(activityContext))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation = previewView.display.rotation

        val cameraSelector = CameraSelector
            .Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview =  Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()



        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(previewView.display.rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmapBuffer =
                Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
            imageProxy.close()

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

                if (isFrontCamera) {
                    postScale(
                        -1f,
                        1f,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat()
                    )
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                matrix, true
            )

            detector.detect(rotatedBitmap)
        }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                findViewTreeLifecycleOwner()!!,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            preview?.setSurfaceProvider(previewView.surfaceProvider)
        } catch(exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    fun clearDetection(){
        detector.clear()
        cameraExecutor.shutdown()
    }

    private fun allPermissionsGranted(activityContext: Activity) = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(activityContext, it) == PackageManager.PERMISSION_GRANTED
    }


    companion object {
        private const val TAG = "YoloV8LiveDetectionView"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf (
            Manifest.permission.CAMERA
        ).toTypedArray()
    }

}
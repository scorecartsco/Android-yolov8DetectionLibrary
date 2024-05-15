package com.surendramaran.yolov8tflite

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.yolov8detectionlibrary.YoloV8LiveDetectionView

class LiveCameraViewTestingActivity : AppCompatActivity() {

    private lateinit var liveView: YoloV8LiveDetectionView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_camera_view_testing)
        liveView = findViewById(R.id.liveView)

        liveView.startDetection(this,"yolov8m_float32.tflite","yolov8_obj_det.txt")


    }

    override fun onDestroy() {
        super.onDestroy()
        liveView.clearDetection()
    }
}
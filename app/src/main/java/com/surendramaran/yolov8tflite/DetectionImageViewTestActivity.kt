package com.surendramaran.yolov8tflite

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.app.yolov8detectionlibrary.DetectionResult
import com.app.yolov8detectionlibrary.YoloV8DetectionImageView

class DetectionImageViewTestActivity : AppCompatActivity() {

    private lateinit var yoloV8DetectionImageView: YoloV8DetectionImageView
    private lateinit var runDetectionButton: Button


    private val results: (detectionResult:DetectionResult)->Unit = {
        when(it){
            DetectionResult.Init -> {
                Log.i("sdf","Init")
            }
            is DetectionResult.OnDetection -> {
                Log.i("sdf","Got Detection")
            }
            DetectionResult.OnDetectionEmpty -> {
                Log.i("sdf","Empty result")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detection_image_view_test)
        yoloV8DetectionImageView = findViewById(R.id.yolov8DetectionImageView)
        runDetectionButton = findViewById(R.id.detectionBtn)
        yoloV8DetectionImageView.setImageFromAssets("Jeddah-AlNahdiPH-AlNahdiPH1043Khuzam_20240429_224412_7220.jpg")

        runDetectionButton.setOnClickListener {

         yoloV8DetectionImageView
             .startDetection(
                 this,
                 Constants.MODEL_PATH,
                 Constants.CLASSES_PATH,
                 results
             )

        }
    }
}
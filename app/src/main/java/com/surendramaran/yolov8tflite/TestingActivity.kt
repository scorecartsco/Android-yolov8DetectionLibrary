package com.surendramaran.yolov8tflite

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.yolov8detectionlibrary.DetectionResult
import com.app.yolov8detectionlibrary.LibUtils
import com.app.yolov8detectionlibrary.YoloV8Detection
import kotlinx.coroutines.launch

class TestingActivity : AppCompatActivity() {

    private lateinit var text: TextView
    private lateinit var image: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)
        image = findViewById(R.id.image)
        text = findViewById(R.id.txt)




        val imageBitmap = LibUtils.loadBitmapFromAssets(this,"shampoo_img_2.jpeg")
        image.setImageBitmap(imageBitmap!!)
        val detection = YoloV8Detection.Builder(this,"shampoo_float32.tflite","shampoo_labels.txt").build()
        detection.detect(imageBitmap)
        lifecycleScope.launch {
            detection.detectionResultFlow.collect{

                when(it){
                    is DetectionResult.OnDetection -> {
                        val boudingBoxes = it.boundingBoxes
                        Log.i("DetectionResults","ResultsLength: ${boudingBoxes.size} with time ${it.inferenceTime}")
                        text.text = ""
                        var labels = ""
                        for(i in boudingBoxes){
                            labels = labels + i.className+"(${i.confidence}),\n"
                        }
                        text.text = labels
                        labels = ""
                    }
                    DetectionResult.OnDetectionEmpty -> {
                        Log.i("DetectionResults","Empty Detection")
                    }
                    else -> {
                        Log.i("DetectionResults","Result Else")

                    }
                }

            }
        }


    }

}
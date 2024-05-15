package com.app.yolov8detectionlibrary

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class YoloV8DetectionImageView(private val context: Context, private val atrSet: AttributeSet): ConstraintLayout(context,atrSet) {
    private lateinit var attributes: TypedArray
    private lateinit var imageView: ImageView
    private lateinit var overlayView: OverlayView

    init {
        initViews()
    }

    private fun initViews(){
        attributes = context.obtainStyledAttributes(atrSet,R.styleable.YoloV8DetectionImageView)
        val layout = LayoutInflater.from(context).inflate(R.layout.detection_image_view,this)
        imageView = layout.findViewById(R.id.imageView)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
            imageView.setImageResource(
                attributes.getSourceResourceId(
                    R.styleable.YoloV8DetectionImageView_setImageDrawable,
                    R.drawable.test2
                )
            )
        }else{
            imageView.setImageDrawable(
                context.resources.getDrawable( attributes.getSourceResourceId(
                    R.styleable.YoloV8DetectionImageView_setImageDrawable,
                    R.drawable.test2
                ))
            )
        }
        overlayView = layout.findViewById(R.id.overlay)

    }

    fun setImageFromResource(resId:Int){
        imageView.setImageResource(resId)
    }
    fun setImageFromAssets(imagePathFromAsset:String){
        imageView.setImageBitmap(LibUtils.loadImageFromAssets(context,imagePathFromAsset))
    }


    fun startDetection(context: AppCompatActivity, modelAssetsPath: String, labelAssetsPath: String , result: ((detectionResult:DetectionResult)->Unit)? = null){
        val imageBitmap = imageView.asBitmap() ?: throw IllegalArgumentException("Image not yet associated with ImageView")

        val detection = YoloV8Detection.Builder(context,modelAssetsPath,labelAssetsPath).build()
            detection.detect(imageBitmap)

        context.lifecycleScope.launch {
            detection.detectionResultFlow.collect{
                if (result != null) {
                    result(it)
                }
                when(it){
                    DetectionResult.Init -> {
                        Log.i(TAG, "initialized detection")
                    }
                    is DetectionResult.OnDetection -> {
                        overlayView.setResults(it.boundingBoxes)
                        overlayView.invalidate()
                    }
                    DetectionResult.OnDetectionEmpty -> {
                        Log.i(TAG, "Empty Detection")
                        overlayView.invalidate()
                    }
                }
            }
        }

    }

    companion object {
        private const val TAG = "YoloV8DetectionIV"
    }


}
package com.app.yolov8detectionlibrary

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * YoloV8DetectionImageView is a custom view for displaying images with YOLOv8 object detection overlay.
 * It provides methods to set image from resource or assets and start object detection on the displayed image.
 *
 * @property context The context in which the view is created.
 * @property atrSet The attribute set for the view.
 */
class YoloV8DetectionImageView(private val context: Context, private val atrSet: AttributeSet): ConstraintLayout(context,atrSet) {
    private lateinit var attributes: TypedArray
    private lateinit var imageView: ImageView
    private lateinit var overlayView: OverlayView

    /**
     * Initializes the view and its child views.
     */
    init {
        initViews()
    }

    /**
     * Initializes the child views of YoloV8DetectionImageView.
     */
    private fun initViews(){
        attributes = context.obtainStyledAttributes(atrSet,R.styleable.YoloV8DetectionImageView)
        val layout = LayoutInflater.from(context).inflate(R.layout.detection_image_view,this)
        imageView = layout.findViewById(R.id.imageView)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
            imageView.setImageResource(
                attributes.getSourceResourceId(
                    R.styleable.YoloV8DetectionImageView_setImageDrawable,
                    R.drawable.oil_replacement10
                )
            )
        }else{
            imageView.setImageDrawable(
                context.resources.getDrawable( attributes.getSourceResourceId(
                    R.styleable.YoloV8DetectionImageView_setImageDrawable,
                    R.drawable.oil_replacement10
                ))
            )
        }
        overlayView = layout.findViewById(R.id.overlay)

    }

    /**
     * Sets the image of YoloV8DetectionImageView from the given resource ID.
     *
     * @param resId The resource ID of the image.
     */
    fun setImageFromResource(resId:Int){
        imageView.setImageResource(resId)
    }

    /**
     * Sets the image of YoloV8DetectionImageView from the specified asset path.
     *
     * @param imagePathFromAsset The path to the image file in the assets directory.
     */
    fun setImageFromAssets(imagePathFromAsset:String){
        imageView.setImageBitmap(LibUtils.loadBitmapFromAssets(context,imagePathFromAsset))
    }

    /**
     * Starts object detection on the displayed image.
     *
     * @param context The context of the calling activity.
     * @param modelAssetsPath The path to the YOLOv8 model file in the assets directory.
     * @param labelAssetsPath The path to the label file in the assets directory.
     * @param result Callback function to receive the detection result.
     */

    fun startDetection(context: AppCompatActivity, modelAssetsPath: String, labelAssetsPath: String , result: ((detectionResult:DetectionResult)->Unit)? = null){
        val imageBitmap = imageView.asBitmap() ?: throw IllegalArgumentException("Image not yet associated with ImageView")

        val detection = YoloV8Detection.Builder(context,modelAssetsPath,labelAssetsPath).build()
            detection.detect(imageBitmap);

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
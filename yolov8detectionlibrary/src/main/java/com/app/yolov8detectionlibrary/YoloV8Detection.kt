package com.app.yolov8detectionlibrary


import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
/**
 * Represents different states of object detection.
 */
sealed class DetectionResult{


    /**
     * Initial state of object detection.
     */
    data object Init:DetectionResult()


    /**
     * Represents the result of object detection.
     *
     * @property boundingBoxes List of detected bounding boxes.
     * @property inferenceTime The time taken for inference.
     */
    data class OnDetection(val boundingBoxes: List<BoundingBox>, val inferenceTime: Long):DetectionResult()


    /**
     * Represents an empty detection result.
     */
    data object OnDetectionEmpty : DetectionResult()
}

/**
 * YoloV8Detection is a class responsible for performing object detection using the YOLOv8 model.
 * It loads the YOLOv8 model from the provided path and performs object detection on input frames.
 * The class provides methods to clear detection results and perform object detection on bitmap frames.
 *
 * @property context The context of the calling activity.
 * @property modelPath The path to the YOLOv8 model file in the assets directory.
 * @property labelPath The path to the label file in the assets directory.
 */

class YoloV8Detection private constructor(
    private val context: AppCompatActivity,
    private val modelPath: String,
    private val labelPath: String,
) {

    private val detectionResult = MutableStateFlow<DetectionResult>(DetectionResult.Init)
    val detectionResultFlow = detectionResult.asStateFlow()

    private var interpreter: Interpreter? = null
    private var labels = mutableListOf<String>()

    private var tensorWidth = 0
    private var tensorHeight = 0
    private var numChannel = 0
    private var numElements = 0

    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()


    /**
     * Initializes the YoloV8Detection instance by setting up the YOLOv8 model and label file.
     */
    init {
      setup()
    }


    /**
     * Sets up the YOLOv8 model and label file by loading them from the provided paths.
     */
    private fun setup(){
        val model = FileUtil.loadMappedFile(context, modelPath)
        val options = Interpreter.Options()
        options.numThreads = 4
        interpreter = Interpreter(model, options)

        val inputShape = interpreter?.getInputTensor(0)?.shape() ?:return
        val outputShape = interpreter?.getOutputTensor(0)?.shape() ?: return

        tensorWidth = inputShape[1]
        tensorHeight = inputShape[2]
        numChannel = outputShape[1]
        numElements = outputShape[2]

        try {
            val inputStream: InputStream = context.assets.open(labelPath)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String? = reader.readLine()
            while (line != null && line != "") {
                labels.add(line)
                line = reader.readLine()
            }

            reader.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    /**
     * Clears the detection results and releases resources associated with the YOLOv8 model.
     */
    fun clear() {
        interpreter?.close()
        interpreter = null
    }



    /**
     * Performs object detection on the provided bitmap frame.
     *
     * @param frame The bitmap frame for object detection.
     */
    fun detect(frame: Bitmap) {
        interpreter ?: return
        if (tensorWidth == 0) return
        if (tensorHeight == 0) return
        if (numChannel == 0) return
        if (numElements == 0) return

        var inferenceTime = SystemClock.uptimeMillis()

        val resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false)

        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(resizedBitmap)
        val processedImage = imageProcessor.process(tensorImage)
        val imageBuffer = processedImage.buffer

        val output = TensorBuffer.createFixedSize(intArrayOf(1 , numChannel, numElements), OUTPUT_IMAGE_TYPE)
        interpreter?.run(imageBuffer, output.buffer)


        val bestBoxes = bestBox(output.floatArray)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime

        if (bestBoxes == null) {

                context.lifecycleScope.launch {
                    detectionResult.emit(DetectionResult.OnDetectionEmpty)
                }

            return
        }
            context.lifecycleScope.launch {
                detectionResult.emit(DetectionResult.OnDetection(bestBoxes, inferenceTime))
            }


    }



    /**
     * Computes the bounding boxes with the highest confidence level among the detected objects.
     *
     * @param array The output array from the YOLOv8 model.
     * @return A list of bounding boxes with the highest confidence level.
     */
    private fun bestBox(array: FloatArray) : List<BoundingBox>? {

        val boundingBoxes = mutableListOf<BoundingBox>()

        for (c in 0 until numElements) {
            var maxConf = -1.0f
            var maxIdx = -1
            var j = 4
            var arrayIdx = c + numElements * j
            while (j < numChannel){
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx]
                    maxIdx = j - 4
                }
                j++
                arrayIdx += numElements
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                val clsName = labels[maxIdx]
                val cx = array[c] // 0
                val cy = array[c + numElements] // 1
                val w = array[c + numElements * 2]
                val h = array[c + numElements * 3]
                val x1 = cx - (w/2F)
                val y1 = cy - (h/2F)
                val x2 = cx + (w/2F)
                val y2 = cy + (h/2F)
                if (x1 < 0F || x1 > 1F) continue
                if (y1 < 0F || y1 > 1F) continue
                if (x2 < 0F || x2 > 1F) continue
                if (y2 < 0F || y2 > 1F) continue

                boundingBoxes.add(
                    BoundingBox(
                        x1 = x1, y1 = y1, x2 = x2, y2 = y2,
                        cx = cx, cy = cy, w = w, h = h,
                        confidence = maxConf, cls = maxIdx, className = clsName
                    )
                )
            }
        }

        if (boundingBoxes.isEmpty()) return null

        return applyNMS(boundingBoxes)
    }



    /**
     * Applies Non-Maximum Suppression (NMS) to remove redundant bounding boxes.
     *
     * @param boxes The list of bounding boxes to apply NMS on.
     * @return The list of selected bounding boxes after applying NMS.
     */
    private fun applyNMS(boxes: List<BoundingBox>) : MutableList<BoundingBox> {
        val sortedBoxes = boxes.sortedByDescending { it.confidence }.toMutableList()
        val selectedBoxes = mutableListOf<BoundingBox>()

        while(sortedBoxes.isNotEmpty()) {
            val first = sortedBoxes.first()
            selectedBoxes.add(first)
            sortedBoxes.remove(first)

            val iterator = sortedBoxes.iterator()
            while (iterator.hasNext()) {
                val nextBox = iterator.next()
                val iou = calculateIoU(first, nextBox)
                if (iou >= IOU_THRESHOLD) {
                    iterator.remove()
                }
            }
        }

        return selectedBoxes
    }



    /**
     * Calculates the Intersection over Union (IoU) between two bounding boxes.
     *
     * @param box1 The first bounding box.
     * @param box2 The second bounding box.
     * @return The IoU value between the two bounding boxes.
     */
    private fun calculateIoU(box1: BoundingBox, box2: BoundingBox): Float {
        val x1 = maxOf(box1.x1, box2.x1)
        val y1 = maxOf(box1.y1, box2.y1)
        val x2 = minOf(box1.x2, box2.x2)
        val y2 = minOf(box1.y2, box2.y2)
        val intersectionArea = maxOf(0F, x2 - x1) * maxOf(0F, y2 - y1)
        val box1Area = box1.w * box1.h
        val box2Area = box2.w * box2.h
        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }

    companion object {
        private const val INPUT_MEAN = 0f
        private const val INPUT_STANDARD_DEVIATION = 255f
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32
        private const val CONFIDENCE_THRESHOLD = 0.3F
        private const val IOU_THRESHOLD = 0.5F
    }




    /**
     * Builder class for creating instances of [YoloV8Detection].
     *
     * @property context The context of the calling activity.
     * @property modelPath The path to the YOLOv8 model file in the assets directory.
     * @property labelPath The path to the label file in the assets directory.
     */
    class Builder(private val context: AppCompatActivity, private val modelPath: String, private val labelPath: String) {
        private var coroutineScope: CoroutineScope? = null



        /**
         * Builds and returns an instance of [YoloV8Detection] using the provided context, model path, and label path.
         *
         * @return An instance of [YoloV8Detection].
         */
        fun build() = YoloV8Detection(context, modelPath, labelPath)
    }
}
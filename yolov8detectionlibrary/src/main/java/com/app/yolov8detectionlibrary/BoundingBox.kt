package com.app.yolov8detectionlibrary


/**
 * Represents a bounding box containing information about a detected object.
 *
 * @property x1 The x-coordinate of the top-left corner of the bounding box.
 * @property y1 The y-coordinate of the top-left corner of the bounding box.
 * @property x2 The x-coordinate of the bottom-right corner of the bounding box.
 * @property y2 The y-coordinate of the bottom-right corner of the bounding box.
 * @property cx The x-coordinate of the center of the bounding box.
 * @property cy The y-coordinate of the center of the bounding box.
 * @property w The width of the bounding box.
 * @property h The height of the bounding box.
 * @property confidence The confidence score of the detected object.
 * @property cls The index of the detected object class.
 * @property className The name of the detected object class.
 */
data class BoundingBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val cx: Float,
    val cy: Float,
    val w: Float,
    val h: Float,
    val confidence: Float,
    val cls: Int,
    val className: String
)
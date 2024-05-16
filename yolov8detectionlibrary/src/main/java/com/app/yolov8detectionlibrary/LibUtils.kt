package com.app.yolov8detectionlibrary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.io.InputStream

/**
 * Utility object for common image-related functions.
 */
object LibUtils {

    /**
     * Loads an image from the assets directory and converts it into a Bitmap.
     *
     * @param context The context used to access the application's assets.
     * @param fileName The name of the image file in the assets directory.
     * @return The Bitmap representation of the loaded image, or null if an error occurs.
     */
     fun loadImageFromAssets(context: Context, fileName: String): Bitmap? {
        return try {
            // Get the AssetManager
            val assetManager = context.assets

            // Open the image file as an InputStream
            val inputStream: InputStream = assetManager.open(fileName)

            // Decode the InputStream into a Bitmap
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            // Handle any errors that may occur
            e.printStackTrace()
            null
        }
    }
}
package com.app.yolov8detectionlibrary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.io.InputStream

object LibUtils {

    // Function to load an image from assets and convert it into a Bitmap
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
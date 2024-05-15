package com.app.yolov8detectionlibrary

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView


fun ImageView.asBitmap():Bitmap?{
        // Get the drawable from the ImageView
        val drawable: Drawable? = this.drawable
        return if(drawable == null) null else (drawable as BitmapDrawable).bitmap
}

package com.app.yolov8detectionlibrary

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html.ImageGetter
import android.widget.ImageView


/**
 * Converts the ImageView's drawable to a Bitmap.
 *
 * @return The Bitmap representation of the ImageView's drawable, or null if the drawable is null.
 */
fun ImageView.asBitmap():Bitmap?{
        // Get the drawable from the ImageView
        val drawable: Drawable? = this.drawable
        return if(drawable == null) null else (drawable as BitmapDrawable).bitmap
}
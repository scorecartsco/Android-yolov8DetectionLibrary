
---

# YOLOv8 Detection Library for Android

## Overview

The YOLOv8 Detection Library is a collection of custom views and utility classes for integrating YOLOv8 object detection into Android applications. This library provides functionality to perform real-time object detection using the YOLOv8 model on live camera feed or static images.

## Features

- Real-time object detection using YOLOv8 model (loading from Assets)
- Custom views for displaying bounding boxes and class labels over detected objects using live camera view and static image
- Utility functions for loading images and processing detection results

## Installation

To integrate the YOLOv8 Detection Library into your Android project, follow these steps:

1. Add the following dependency to your `build.gradle` groovy file:

   ```groovy
   implementation 'com.github.scorecartsco:Android-yolov8DetectionLibrary:latest-version'
   ```
   OR for kotlin file : 

 ```kotlin
   implementation ("com.github.scorecartsco:Android-yolov8DetectionLibrary:${latest-version}")
   ```
   

2. Sync your project with Gradle.

## Usage

### Live Camera View

To use the live camera view for real-time object detection, follow these steps:

1. Add the `YoloV8LiveDetectionView` to your layout XML file:

   ```xml
   <com.app.yolov8detectionlibrary.YoloV8LiveDetectionView
       android:id="@+id/liveDetectionView"
       android:layout_width="match_parent"
       android:layout_height="match_parent" />
   ```

2. Initialize the view in your activity or fragment:

   ```kotlin
   val liveDetectionView = findViewById<YoloV8LiveDetectionView>(R.id.liveDetectionView)
   ```

3. Start detection by calling the `startDetection` method:

   ```kotlin
   liveDetectionView.startDetection(this, modelPath, labelPath)
   ```

### Image View Detection

To perform object detection on a static image using an image view, follow these steps:

1. Add the `YoloV8DetectionImageView` to your layout XML file:

   ```xml
   <com.app.yolov8detectionlibrary.YoloV8DetectionImageView
       android:id="@+id/imageDetectionView"
       android:layout_width="match_parent"
       android:layout_height="match_parent" />
   ```

2. Initialize the view in your activity or fragment:

   ```kotlin
   val imageDetectionView = findViewById<YoloV8DetectionImageView>(R.id.imageDetectionView)
   ```

3. Set the image resource or image path using the appropriate method:

   ```kotlin
   imageDetectionView.setImageFromResource(R.drawable.image)
   // or
   imageDetectionView.setImageFromAssets("image.jpg")
   ```


## Android Device requirement

- Android version 19(Kitkat) and later
- RAM 2 GB and more
- ARM Processor recommended or x86 processor can also be fine
- GPU recommended (but not mandatory)

## Documentation

For detailed documentation on each class and method, refer to the source code or generated documentation.

## Acknowledgments

- YOLOv8 model implementation based on [YOLOv8 GitHub repository](https://github.com/yolov8/yolov8)
- Camera functionality inspired by [CameraX API](https://developer.android.com/training/camerax)

---
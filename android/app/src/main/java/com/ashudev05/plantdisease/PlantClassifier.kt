package com.ashudev05.plantdisease

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.tensorflow.lite.Interpreter
import kotlin.math.max

class PlantClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var isInitialized = false

    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0
    private var modelInputSize: Int = 0

    private val classNames = listOf(
        "Pepper bell - Bacterial spot",
        "Pepper bell - healthy",
        "Potato - Early blight",
        "Potato - Late blight",
        "Potato - healthy",
        "Tomato - Bacterial spot",
        "Tomato - Early blight",
        "Tomato - Late blight",
        "Tomato - Leaf Mold",
        "Tomato - Septoria leaf spot",
        "Tomato - Spider mites - Two spotted spider mite",
        "Tomato - Target Spot",
        "Tomato - Tomato Yellow Leaf - Curl Virus",
        "Tomato - Tomato mosaic virus",
        "Tomato - healthy"
    )

    fun initialize(): Task<Void?> {
        val task = TaskCompletionSource<Void?>()
        executorService.execute {
            try {
                initializeInterpreter()
                task.setResult(null)
            } catch (e: IOException) {
                task.setException(e)
            }
        }
        return task.task
    }

    @Throws(IOException::class)
    private fun initializeInterpreter() {
        val assetManager = context.assets
        val model = loadModelFile(assetManager, "model.tflite")
        val interpreter = Interpreter(model)

        val inputShape = interpreter.getInputTensor(0).shape()  // {1, 256, 256, 3}
        Log.d(TAG, "Input shape: ${inputShape.contentToString()}")
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        modelInputSize = inputImageWidth * inputImageHeight * PIXEL_SIZE

        this.interpreter = interpreter
        isInitialized = true
        Log.d(TAG, "Initialized TFLite interpreter.")
    }

    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, filename: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun classify(bitmap: Bitmap): String {
        check(isInitialized) { "TF Lite Interpreter is not initialized yet." }

        // Pre-processing: resize the input image to match the model input shape.
        val resizedImage = Bitmap.createScaledBitmap(
            bitmap,
            inputImageWidth,
            inputImageHeight,
            true
        )
        val byteBuffer = convertBitmapToByteBuffer(resizedImage)

        // Define an array to store the model output.
        val output = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }

        // Run inference with the input data.
        interpreter?.run(byteBuffer, output)

        // Post-processing: find the output with the highest probability
        val result = output[0]
        var maxIndex = ((result.indices.maxByOrNull { result[it] })?.minus(1)) ?: -1

        // Get the class name from the classNames list using the maxIndex
        val predictedClassName = if (maxIndex >= 0 && maxIndex < classNames.size) {
            classNames[maxIndex]
        } else {
            "Unknown Class"
        }

        maxIndex = if (maxIndex == -1) maxIndex else maxIndex + 1
        val confidence = result[maxIndex] * 100 // Get the confidence score
        val resultString = "Prediction Result: $predictedClassName\nConfidence: %.2f".format(confidence)

        return resultString
    }


    fun classifyAsync(bitmap: Bitmap): Task<String> {
        val task = TaskCompletionSource<String>()
        executorService.execute {
            val result = classify(bitmap)
            task.setResult(result)
        }
        return task.task
    }

    fun close() {
        executorService.execute {
            interpreter?.close()
            Log.d(TAG, "Closed TFLite interpreter.")
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // Resize the bitmap to 256x256 pixels
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true)

        // Allocate the ByteBuffer for float values
        val byteBuffer = ByteBuffer.allocateDirect(256 * 256 * 3 * 4) // 4 bytes per float
        byteBuffer.order(ByteOrder.nativeOrder())

        // Create an array to hold pixel values
        val pixels = IntArray(256 * 256)
        resizedBitmap.getPixels(pixels, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        // Loop through each pixel to extract R, G, B values
        for (pixelValue in pixels) {
            // Extract R, G, B values from the pixel
            val r = (pixelValue shr 16 and 0xFF) / 255.0f // Normalize to [0, 1]
            val g = (pixelValue shr 8 and 0xFF) / 255.0f  // Normalize to [0, 1]
            val b = (pixelValue and 0xFF) / 255.0f        // Normalize to [0, 1]

            // Put the normalized values into the byte buffer
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        return byteBuffer
    }


    companion object {
        private const val TAG = "PlantClassifier"

        private const val PIXEL_SIZE = 3 // R, G, B
        private const val OUTPUT_CLASSES_COUNT = 15
    }
}

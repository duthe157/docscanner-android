package com.example.camscanner.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TFLiteModule {

    @Provides
    @Singleton
    fun provideTFLiteInterpreter(
        @ApplicationContext context: Context
    ): Interpreter? {
        return try {
            val modelBuffer = loadModelFile(context, "models/document_detector.tflite")
            val options = Interpreter.Options().apply {
                numThreads = 2
                useNNAPI = false
            }
            Interpreter(modelBuffer, options)
        } catch (e: Exception) {
            // Model not found or invalid — fallback to ContourDetector
            null
        }
    }

    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}

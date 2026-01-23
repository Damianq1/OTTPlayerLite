package com.ottplayerlite

import android.content.Context
import android.os.Environment
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class RecordingWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val streamUrl = inputData.getString("url") ?: return Result.failure()
        val durationMs = inputData.getLong("duration", 3600000) // domyślnie 1h
        val fileName = inputData.getString("name") ?: "recording"

        val file = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "$fileName.ts")
        
        try {
            val inputStream = URL(streamUrl).openStream()
            val outputStream = FileOutputStream(file)
            val startTime = System.currentTimeMillis()
            val buffer = ByteArray(1024 * 32)
            var bytesRead: Int

            while (System.currentTimeMillis() - startTime < durationMs) {
                bytesRead = inputStream.read(buffer)
                if (bytesRead == -1) break
                outputStream.write(buffer, 0, bytesRead)
            }
            
            outputStream.close()
            inputStream.close()
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}

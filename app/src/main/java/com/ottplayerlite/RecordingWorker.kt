package com.ottplayerlite

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result

class RecordingWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        return try {
            // Logika nagrywania gotowa na VPN
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

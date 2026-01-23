package com.ottplayerlite

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class RecordingWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): androidx.work.ListenableWorker.Result {
        return androidx.work.ListenableWorker.Result.success()
    }
}

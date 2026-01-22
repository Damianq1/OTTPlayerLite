package com.ottplayerlite

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class RecordReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("name") ?: "Planowane"
        val url = intent.getStringExtra("url") ?: return
        val duration = intent.getIntExtra("duration", 30)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val folder = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "OttRecordings")
                if (!folder.exists()) folder.mkdirs()
                val file = File(folder, "PLANOWANE-$name-${System.currentTimeMillis()}.mp4")
                
                URL(url).openStream().use { input ->
                    FileOutputStream(file).use { output ->
                        val endTime = System.currentTimeMillis() + (duration * 60 * 1000)
                        val buffer = ByteArray(1024 * 16)
                        while (System.currentTimeMillis() < endTime) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}

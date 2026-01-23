package com.ottplayerlite

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ottplayerlite.utils.CatchupManager
import java.util.*

class EpgActivity : AppCompatActivity() {
    private val daysList = mutableListOf<Calendar>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epg)

        val channelName = intent.getStringExtra("channel_name") ?: "Kanał"
        findViewById<TextView>(R.id.txtEpgTitle).text = channelName

        setupCalendarData()
        setupRecyclerViews()
    }

    private fun setupCalendarData() {
        // Generujemy listę: 7 dni wstecz, dzisiaj, 7 dni w przód
        for (i in -7..7) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, i)
            daysList.add(cal)
        }
    }

    private fun setupRecyclerViews() {
        val dateBar = findViewById<RecyclerView>(R.id.dateRecyclerView)
        dateBar.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // Tutaj podepniemy adapter dat (wyświetlający np. "Pn 21.01")
        
        val programList = findViewById<RecyclerView>(R.id.programRecyclerView)
        programList.layoutManager = LinearLayoutManager(this)
        // Tutaj podepniemy listę programów z detalami (Sezon/Odcinek)
    }

    private fun onProgramSelected(programStartTime: Long, isArchive: Boolean) {
        if (isArchive) {
            val url = intent.getStringExtra("channel_url") ?: ""
            val archiveUrl = CatchupManager.getArchiveUrl(url, programStartTime)
            // Wyślij archiveUrl do odtwarzacza
            Toast.makeText(this, "Uruchamiam Archiwum...", Toast.LENGTH_SHORT).show()
        }
    }
}

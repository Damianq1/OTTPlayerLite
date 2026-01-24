package com.ottplayerlite

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ottplayerlite.R

class EpgActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epg)

        // 1. Podpięcie tytułu
        val txtTitle = findViewById<TextView>(R.id.txtEpgTitle)
        txtTitle?.text = "Program TV - Ultimate"

        // 2. Podpięcie głównej listy (epgRecyclerView)
        val epgRecycler = findViewById<RecyclerView>(R.id.epgRecyclerView)
        epgRecycler?.layoutManager = LinearLayoutManager(this)

        // 3. Podpięcie listy dat (dateRecyclerView)
        val dateRecycler = findViewById<RecyclerView>(R.id.dateRecyclerView)
        dateRecycler?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 4. Podpięcie listy programów (programRecyclerView)
        val programRecycler = findViewById<RecyclerView>(R.id.programRecyclerView)
        programRecycler?.layoutManager = LinearLayoutManager(this)

        // Logika kliknięcia w archiwum
        val onEpgItemClick: (String) -> Unit = { archiveUrl ->
            if (archiveUrl.isNotEmpty()) {
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra("url", archiveUrl)
                    putExtra("is_archive", true)
                }
                startActivity(intent)
            }
        }
    }
}

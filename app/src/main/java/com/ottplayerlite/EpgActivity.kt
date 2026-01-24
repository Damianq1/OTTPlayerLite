package com.ottplayerlite

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EpgActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epg)

        val recyclerView = findViewById<RecyclerView>(R.id.epgRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Przykład użycia archiveUrl przy kliknięciu w element EPG
        // Zakładamy, że adapter zwraca url do nagrania
        val onEpgItemClick: (String) -> Unit = { archiveUrl ->
            if (archiveUrl.isNotEmpty()) {
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra("url", archiveUrl)
                    putExtra("is_archive", true)
                }
                startActivity(intent)
            }
        }
        
        // Tutaj ładowany byłby Twój adapter EPG
        // recyclerView.adapter = EpgAdapter(epgList, onEpgItemClick)
    }
}

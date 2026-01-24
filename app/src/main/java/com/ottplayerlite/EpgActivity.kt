package com.ottplayerlite

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ottplayerlite.R

class EpgActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epg)

        // POPRAWKA: Definiujemy recyclerView lokalnie i łączymy z XML przez R.id
        val recyclerView = findViewById<RecyclerView>(R.id.epgRecyclerView)
        
        // Sprawdzamy, czy widok istnieje, żeby uniknąć crashu
        recyclerView?.let {
            it.layoutManager = LinearLayoutManager(this)
            // Tutaj możesz przypisać adapter w przyszłości
            // it.adapter = EpgAdapter(...) 
        }

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

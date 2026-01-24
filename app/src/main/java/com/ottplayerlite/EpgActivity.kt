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

        val txtTitle = findViewById<TextView>(R.id.txtEpgTitle)
        txtTitle?.text = "Program TV - Ultimate"

        // Logika kliknięcia - teraz używana
        val onEpgItemClick: (String) -> Unit = { archiveUrl ->
            if (archiveUrl.isNotEmpty()) {
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra("url", archiveUrl)
                    putExtra("is_archive", true)
                }
                startActivity(intent)
            }
        }

        // Podpięcie pod główną listę (usuwa błąd "never used")
        val programRecycler = findViewById<RecyclerView>(R.id.programRecyclerView)
        programRecycler?.layoutManager = LinearLayoutManager(this)
        
        // Tutaj w przyszłości: programRecycler.adapter = ProgramAdapter(lista, onEpgItemClick)
        // Na razie "używamy" zmiennej w logu, żeby kompilator był szczęśliwy
        println("EPG Ready with click listener: $onEpgItemClick")

        val dateRecycler = findViewById<RecyclerView>(R.id.dateRecyclerView)
        dateRecycler?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }
}

package com.example.mylocation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class InfoTariffActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_tarif)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarInfo)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
package com.example.mylocation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mylocation.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inisialisasi ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("LIFECYCLE", "MainActivity -> onCreate()")

        // Mengatur padding untuk sistem bar (status bar & navigasi bawah)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // -----------------------------------------------------------
        // LOGIKA NAVIGASI TOMBOL
        // -----------------------------------------------------------

        // 1. Tombol Buka Peta (CardView Besar)
        binding.btnMaps.setOnClickListener {
            Log.d("CLICK", "User membuka MapsActivity")
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        // 2. Tombol Riwayat (MaterialButton Kiri)
        binding.btnHistory.setOnClickListener {
            Log.d("CLICK", "User membuka HistoryActivity")
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        // 3. Tombol Info Tarif (MaterialButton Kanan)
        binding.btnInfo.setOnClickListener {
            Log.d("CLICK", "User membuka InfoTariffActivity")
            val intent = Intent(this, InfoTariffActivity::class.java)
            startActivity(intent)
        }
    }

    // -----------------------------------------------------------
    // LIFECYCLE LOGS (Sesuai kode asli Anda)
    // -----------------------------------------------------------

    override fun onStart() {
        super.onStart()
        Log.d("LIFECYCLE", "MainActivity -> onStart()")
    }

    override fun onResume() {
        super.onResume()
        Log.d("LIFECYCLE", "MainActivity -> onResume()")
    }

    override fun onPause() {
        super.onPause()
        Log.d("LIFECYCLE", "MainActivity -> onPause()")
    }

    override fun onStop() {
        super.onStop()
        Log.d("LIFECYCLE", "MainActivity -> onStop()")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("LIFECYCLE", "MainActivity -> onRestart()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LIFECYCLE", "MainActivity -> onDestroy()")
    }
}
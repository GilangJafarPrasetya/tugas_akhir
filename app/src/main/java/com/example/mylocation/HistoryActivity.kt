package com.example.mylocation

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarHistory)
        toolbar.setNavigationOnClickListener { finish() }

        // 1. Ambil Wadah (Container)
        val container = findViewById<LinearLayout>(R.id.llHistoryContainer)

        // 2. Ambil Data dari Helper
        val historyList = HistoryHelper.getHistoryList(this)

        // 3. Cek jika data kosong
        if (historyList.isEmpty()) {
            // Tampilkan teks kosong jika belum ada riwayat (Opsional)
            // (Bisa ditambahkan TextView "Belum ada riwayat" secara manual di sini)
        }

        // 4. Loop (Perulangan) untuk menampilkan setiap data
        for (data in historyList) {
            val namaLokasi = data.first
            val waktuKunjungan = data.second

            // Inflate (Membuat) tampilan dari item_history.xml
            val view = LayoutInflater.from(this).inflate(R.layout.item_history, container, false)

            // Isi Teks
            val tvName = view.findViewById<TextView>(R.id.tvHistoryName)
            val tvDate = view.findViewById<TextView>(R.id.tvHistoryDate)

            tvName.text = namaLokasi
            tvDate.text = waktuKunjungan

            // Masukkan ke dalam container
            container.addView(view)
        }
    }
}
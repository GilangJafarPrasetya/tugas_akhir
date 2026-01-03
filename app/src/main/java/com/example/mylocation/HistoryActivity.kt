package com.example.mylocation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var dbHistory: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarHistory)
        toolbar.setNavigationOnClickListener { finish() }

        val container = findViewById<LinearLayout>(R.id.llHistoryContainer)

        // 1. Inisialisasi Database dengan URL Singapore
        dbHistory = FirebaseDatabase.getInstance("https://tugaspmob-16c5b-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("user_history")

        // 2. Ambil data dari Firebase (Real-time)
        dbHistory.orderByChild("waktu").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Bersihkan list sebelum update agar tidak terjadi duplikasi
                container.removeAllViews()

                // Iterasi data dan balik urutannya agar riwayat terbaru di atas
                val listHistory = mutableListOf<DataSnapshot>()
                for (dataSnap in snapshot.children) {
                    listHistory.add(dataSnap)
                }
                listHistory.reverse()

                for (dataSnap in listHistory) {
                    val nama = dataSnap.child("nama").value.toString()
                    val waktu = dataSnap.child("waktu").value.toString()

                    // Inflate tampilan dari item_history.xml
                    val view = LayoutInflater.from(this@HistoryActivity)
                        .inflate(R.layout.item_history, container, false)

                    // Isi teks ke TextView
                    val tvName = view.findViewById<TextView>(R.id.tvHistoryName)
                    val tvDate = view.findViewById<TextView>(R.id.tvHistoryDate)

                    tvName.text = nama
                    tvDate.text = waktu

                    // Masukkan ke dalam container
                    container.addView(view)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE_ERROR", "Gagal mengambil riwayat: ${error.message}")
            }
        })
    }
}
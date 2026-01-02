package com.example.mylocation

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoryHelper {
    private const val PREF_NAME = "ParkirHistoryRefs"
    private const val KEY_DATA = "history_data_list"

    // Fungsi untuk Menyimpan Data Baru
    fun saveHistory(context: Context, parkirName: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Ambil data lama
        val oldData = sharedPreferences.getStringSet(KEY_DATA, mutableSetOf()) ?: mutableSetOf()

        // Buat data baru (Format: NamaLokasi|Waktu)
        val timestamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        val newData = "$parkirName|$timestamp"

        // Gabungkan dan simpan
        oldData.add(newData)
        sharedPreferences.edit().putStringSet(KEY_DATA, oldData).apply()
    }

    // Fungsi untuk Mengambil Semua Data
    fun getHistoryList(context: Context): List<Pair<String, String>> {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val rawData = sharedPreferences.getStringSet(KEY_DATA, setOf()) ?: return emptyList()

        // Ubah data string menjadi List agar bisa ditampilkan
        val list = mutableListOf<Pair<String, String>>()
        for (item in rawData) {
            val parts = item.split("|")
            if (parts.size == 2) {
                list.add(Pair(parts[0], parts[1]))
            }
        }
        // Urutkan biar yang terbaru di atas (opsional, sort by string agak tricky, ini basic)
        return list.reversed()
    }

    // Fungsi Hapus Riwayat (Opsional)
    fun clearHistory(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
}
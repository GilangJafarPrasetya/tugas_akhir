package com.example.mylocation

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

object HistoryHelper {
    private val dbHistory = FirebaseDatabase.getInstance().getReference("user_history")

    fun saveHistory(context: Context, parkirName: String) {
        val timestamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

        // Simpan ke Firebase
        val historyId = dbHistory.push().key ?: return
        val data = mapOf("nama" to parkirName, "waktu" to timestamp)
        dbHistory.child(historyId).setValue(data)

        // Simpan lokal (SharedPreferences) sebagai cadangan
        val sharedPref = context.getSharedPreferences("ParkirHistoryRefs", Context.MODE_PRIVATE)
        val oldData = sharedPref.getStringSet("history_data_list", mutableSetOf()) ?: mutableSetOf()
        oldData.add("$parkirName|$timestamp")
        sharedPref.edit().putStringSet("history_data_list", oldData).apply()
    }

    fun getHistoryList(context: Context): List<Pair<String, String>> {
        val sharedPref = context.getSharedPreferences("ParkirHistoryRefs", Context.MODE_PRIVATE)
        val rawData = sharedPref.getStringSet("history_data_list", setOf()) ?: return emptyList()
        return rawData.map {
            val parts = it.split("|")
            Pair(parts[0], parts[1])
        }.sortedByDescending { it.second } // Urutkan terbaru
    }
}
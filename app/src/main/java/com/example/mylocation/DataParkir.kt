package com.example.mylocation

import com.google.android.gms.maps.model.LatLng

data class DataParkir(
    val nama: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val status: String = "",
    val hargaMotor: Int = 0,
    val hargaMobil: Int = 0
) {
    // Properti pembantu untuk konversi LatLng (Firebase tidak bisa menyimpan objek LatLng secara langsung)
    val lokasi: LatLng get() = LatLng(lat, lng)
}
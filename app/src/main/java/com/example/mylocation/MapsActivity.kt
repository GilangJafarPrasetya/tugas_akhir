package com.example.mylocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mylocation.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.NumberFormat
import java.util.Locale

// Membuat Model Data Custom agar bisa menampung harga
data class DataParkir(
    val nama: String,
    val lokasi: LatLng,
    val status: String,
    val hargaMotor: Int,
    val hargaMobil: Int
)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var myLocation: Location? = null

    // Simpan objek DataParkir lengkap saat dipilih
    private var selectedParkingData: DataParkir? = null

    private val FINE_PERMISSION_CODE = 1

    // Update Daftar Lokasi dengan Harga (Sesuai Zona Jogja)
    private val lokasiParkir = listOf(
        DataParkir("Parkir Malioboro (Utara)", LatLng(-7.7930, 110.3660), "Masih Ada", 4000, 5000),
        DataParkir("Parkir Abu Bakar Ali", LatLng(-7.7895, 110.3670), "Penuh", 4000, 5000),
        DataParkir("Parkir Ngabean", LatLng(-7.8032, 110.3602), "Masih Ada", 4000, 5000),
        DataParkir("Parkir Senopati", LatLng(-7.7970, 110.3710), "Masih Ada", 4000, 5000),
        DataParkir("Parkir Kridosono", LatLng(-7.7848, 110.3772), "Penuh", 2000, 5000),
        DataParkir("Parkir XT Square", LatLng(-7.8145, 110.3840), "Masih Ada", 2000, 3000),
        DataParkir("Parkir Gembira Loka", LatLng(-7.7999, 110.3983), "Masih Ada", 3000, 5000),
        DataParkir("Parkir Taman Pintar", LatLng(-7.8009, 110.3690), "Penuh", 4000, 5000),
        DataParkir("Parkir Stasiun Tugu", LatLng(-7.7891, 110.3633), "Masih Ada", 5000, 10000),
        DataParkir("Parkir Alun-Alun Utara", LatLng(-7.8039, 110.3649), "Penuh", 3000, 5000)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkPermissionAndGetLocation()

        // TOMBOL ARAHKAN
        binding.btnArahkan.setOnClickListener {
            if (selectedParkingData != null) {
                // Simpan ke history dan navigasi
                val data = selectedParkingData!!
                HistoryHelper.saveHistory(this, data.nama)
                openGoogleMapsRoute(data.lokasi)
            } else {
                findNearestParking()
            }
        }

        binding.btnZoomIn.setOnClickListener { mMap.animateCamera(CameraUpdateFactory.zoomIn()) }
        binding.btnZoomOut.setOnClickListener { mMap.animateCamera(CameraUpdateFactory.zoomOut()) }
    }

    private fun checkPermissionAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_PERMISSION_CODE
            )
        } else {
            enableUserLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        if (::mMap.isInitialized) mMap.isMyLocationEnabled = true

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                myLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13f))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }

        // Loop data menggunakan Class DataParkir
        for (data in lokasiParkir) {
            val warna = if (data.status == "Masih Ada")
                BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED

            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(data.lokasi)
                    .title(data.nama)
                    // Menampilkan harga langsung di snippet kecil di atas marker
                    .snippet("Motor: ${formatRupiah(data.hargaMotor)} | Mobil: ${formatRupiah(data.hargaMobil)}")
                    .icon(BitmapDescriptorFactory.defaultMarker(warna))
            )
            // Simpan seluruh objek DataParkir ke dalam tag marker
            marker?.tag = data
        }

        val yogyakarta = LatLng(-7.7956, 110.3695)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yogyakarta, 13f))

        // Klik Marker -> Ambil data lengkap
        mMap.setOnMarkerClickListener { marker ->
            val data = marker.tag as? DataParkir // Casting ke DataParkir

            if (data != null) {
                selectedParkingData = data // Simpan pilihan global
                showParkingInfoDialog(data) // Tampilkan Dialog Lengkap
            }
            false
        }

        mMap.setOnMapClickListener {
            selectedParkingData = null
            Toast.makeText(this, "Silakan pilih lokasi parkir (Marker)", Toast.LENGTH_SHORT).show()
        }
    }

    //Dialog Informasi Lengkap dengan Harga
    private fun showParkingInfoDialog(data: DataParkir) {
        if (myLocation == null) {
            Toast.makeText(this, "Sedang mencari lokasi Anda...", Toast.LENGTH_SHORT).show()
            return
        }

        // Hitung Jarak
        val result = FloatArray(1)
        Location.distanceBetween(
            myLocation!!.latitude, myLocation!!.longitude,
            data.lokasi.latitude, data.lokasi.longitude,
            result
        )
        val jarakKm = result[0] / 1000

        // Hitung Waktu
        val waktuMenit = ((jarakKm / 30.0) * 60).toInt()

        val builder = AlertDialog.Builder(this)
        builder.setTitle(data.nama)

        // Isi pesan dengan Harga
        builder.setMessage(
            "Status: ${data.status}\n" +
                    "Jarak: ${String.format("%.2f", jarakKm)} km\n" +
                    "Estimasi Waktu: $waktuMenit menit\n\n" +
                    "💰 TARIF PARKIR:\n" +
                    "🏍️ Motor: ${formatRupiah(data.hargaMotor)}\n" +
                    "🚗 Mobil: ${formatRupiah(data.hargaMobil)}"
        )

        builder.setPositiveButton("Buka Rute") { _, _ ->
            HistoryHelper.saveHistory(this, data.nama)
            Toast.makeText(this, "Disimpan ke Riwayat", Toast.LENGTH_SHORT).show()
            openGoogleMapsRoute(data.lokasi)
        }

        builder.setNegativeButton("Detail") { _, _ ->
            val intent = Intent(this, DetailParkirActivity::class.java)
            intent.putExtra("nama", data.nama)
            intent.putExtra("koordinat", "${data.lokasi.latitude}, ${data.lokasi.longitude}")
            intent.putExtra("status", data.status)
            // Kirim juga data harga jika diperlukan di detail
            intent.putExtra("harga_motor", data.hargaMotor)
            intent.putExtra("harga_mobil", data.hargaMobil)
            startActivity(intent)
        }

        builder.setNeutralButton("Tutup", null)
        builder.show()
    }

    private fun openGoogleMapsRoute(dest: LatLng) {
        val gmmIntentUri = Uri.parse("google.navigation:q=${dest.latitude},${dest.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(this, "Google Maps tidak ditemukan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findNearestParking() {
        if (myLocation == null) {
            Toast.makeText(this, "Lokasi belum ditemukan.", Toast.LENGTH_SHORT).show()
            return
        }

        var nearestDist = Float.MAX_VALUE
        var nearestSpot: DataParkir? = null

        for (spot in lokasiParkir) {
            val result = FloatArray(1)
            Location.distanceBetween(
                myLocation!!.latitude, myLocation!!.longitude,
                spot.lokasi.latitude, spot.lokasi.longitude,
                result
            )
            if (result[0] < nearestDist) {
                nearestDist = result[0]
                nearestSpot = spot
            }
        }

        if (nearestSpot != null) {
            showParkingInfoDialog(nearestSpot)
        }
    }

    // Helper Format Rupiah
    private fun formatRupiah(number: Int): String {
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        return formatRupiah.format(number.toDouble()).replace("Rp", "Rp ").replace(",00", "")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkPermissionAndGetLocation()
        }
    }
}
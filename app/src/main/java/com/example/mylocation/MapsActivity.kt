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
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var dbRef: DatabaseReference

    private var myLocation: Location? = null
    private var selectedParkingData: DataParkir? = null
    private val lokasiParkirList = mutableListOf<DataParkir>()

    // Koordinat Pusat Kota Yogyakarta (Tugu Jogja)
    private val PUSAT_JOGJA = LatLng(-7.7829, 110.3671)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Referensi ke Firebase (Sesuai dengan struktur JSON yang kita buat)
        dbRef = FirebaseDatabase.getInstance("https://tugaspmob-16c5b-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("lokasi_parkir")
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Tombol Arahkan: Navigasi ke lokasi yang dipilih atau cari yang terdekat
        binding.btnArahkan.setOnClickListener {
            selectedParkingData?.let {
                HistoryHelper.saveHistory(this, it.nama)
                openGoogleMapsRoute(it.lokasi)
            } ?: findNearestParking()
        }

        binding.btnZoomIn.setOnClickListener { mMap.animateCamera(CameraUpdateFactory.zoomIn()) }
        binding.btnZoomOut.setOnClickListener { mMap.animateCamera(CameraUpdateFactory.zoomOut()) }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 1. Fokuskan Kamera langsung ke Yogyakarta saat peta siap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PUSAT_JOGJA, 14f))

        // 2. Aktifkan lokasi pengguna (Tombol "titik biru")
        checkPermissionAndGetLocation()

        // 3. Ambil data dari Firebase dan buat tanda lokasi (Marker)
        fetchParkingFromFirebase()
    }

    private fun fetchParkingFromFirebase() {
        // addValueEventListener membuat peta terupdate otomatis (Real-time) jika status di Firebase berubah
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mMap.clear() // Bersihkan peta sebelum menggambar ulang
                lokasiParkirList.clear()

                for (dataSnap in snapshot.children) {
                    val p = dataSnap.getValue(DataParkir::class.java) ?: continue
                    lokasiParkirList.add(p)

                    // Warna Marker: Hijau (Masih Ada), Merah (Penuh)
                    val color = if (p.status == "Masih Ada")
                        BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED

                    // Tambahkan Marker ke Peta
                    val marker = mMap.addMarker(MarkerOptions()
                        .position(p.lokasi)
                        .title(p.nama)
                        .snippet("Status: ${p.status}")
                        .icon(BitmapDescriptorFactory.defaultMarker(color)))

                    // Simpan data objek ke dalam marker untuk diambil saat diklik
                    marker?.tag = p
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapsActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Aksi saat Marker diklik
        mMap.setOnMarkerClickListener { marker ->
            val data = marker.tag as? DataParkir
            if (data != null) {
                selectedParkingData = data
                showParkingInfoDialog(data)
            }
            false
        }
    }

    private fun showParkingInfoDialog(data: DataParkir) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(data.nama)
        builder.setMessage(
            "Status: ${data.status}\n\n" +
                    "Tarif Parkir:\n" +
                    "🏍️ Motor: ${formatRupiah(data.hargaMotor)}\n" +
                    "🚗 Mobil: ${formatRupiah(data.hargaMobil)}"
        )
        builder.setPositiveButton("Buka Rute") { _, _ ->
            HistoryHelper.saveHistory(this, data.nama)
            openGoogleMapsRoute(data.lokasi)
        }
        builder.setNegativeButton("Detail") { _, _ ->
            val intent = Intent(this, DetailParkirActivity::class.java).apply {
                putExtra("nama", data.nama)
                putExtra("koordinat", "${data.lat}, ${data.lng}")
                putExtra("status", data.status)
                putExtra("harga_motor", data.hargaMotor)
                putExtra("harga_mobil", data.hargaMobil)
            }
            startActivity(intent)
        }
        builder.setNeutralButton("Tutup", null)
        builder.show()
    }

    private fun openGoogleMapsRoute(dest: LatLng) {
        val uri = Uri.parse("google.navigation:q=${dest.latitude},${dest.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Aplikasi Google Maps tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatRupiah(number: Int): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        return format.format(number.toDouble()).replace(",00", "")
    }

    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        mMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                myLocation = location
                // Opsional: Jika ingin kamera langsung ke lokasi user, aktifkan baris bawah
                // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
            }
        }
    }

    private fun checkPermissionAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun findNearestParking() {
        if (myLocation == null) {
            Toast.makeText(this, "Lokasi Anda belum terdeteksi", Toast.LENGTH_SHORT).show()
            return
        }
        var nearest: DataParkir? = null
        var minDistance = Float.MAX_VALUE
        for (spot in lokasiParkirList) {
            val res = FloatArray(1)
            Location.distanceBetween(myLocation!!.latitude, myLocation!!.longitude, spot.lat, spot.lng, res)
            if (res[0] < minDistance) {
                minDistance = res[0]
                nearest = spot
            }
        }
        nearest?.let { showParkingInfoDialog(it) }
    }
}
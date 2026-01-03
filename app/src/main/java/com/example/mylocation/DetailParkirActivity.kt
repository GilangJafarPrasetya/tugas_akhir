package com.example.mylocation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mylocation.databinding.ActivityDetailParkirBinding
import java.text.NumberFormat
import java.util.Locale

class DetailParkirActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailParkirBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailParkirBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nama = intent.getStringExtra("nama")
        val koordinat = intent.getStringExtra("koordinat")
        val status = intent.getStringExtra("status")
        val hMotor = intent.getIntExtra("harga_motor", 0)
        val hMobil = intent.getIntExtra("harga_mobil", 0)

        binding.txtNamaLokasi.text = nama
        binding.txtKoordinat.text = "Koordinat: $koordinat"
        binding.txtStatus.text = "Status: $status"
        binding.txtTarif.text = "Motor: ${formatRupiah(hMotor)} | Mobil: ${formatRupiah(hMobil)}"
        binding.txtJam.text = "Jam Operasional: 07.00 - 22.00"
    }

    private fun formatRupiah(number: Int): String {
        return NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(number.toDouble()).replace(",00", "")
    }
}
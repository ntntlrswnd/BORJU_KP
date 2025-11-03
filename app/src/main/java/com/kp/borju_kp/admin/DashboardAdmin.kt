package com.kp.borju_kp.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.kp.borju_kp.R
import java.text.NumberFormat
import java.util.Locale

class DashboardAdmin : AppCompatActivity() {

    private lateinit var tvJumlahPesanan: TextView
    private lateinit var tvLabaBersih: TextView
    private lateinit var tvTotalPengeluaran: TextView

    private val db = FirebaseFirestore.getInstance()
    private var ordersListener: ListenerRegistration? = null
    private var expensesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_admin)
        enableEdgeToEdge()

        tvJumlahPesanan = findViewById(R.id.tv_jumlah_pesanan_value)
        tvLabaBersih = findViewById(R.id.tv_laba_bersih_value)
        tvTotalPengeluaran = findViewById(R.id.tv_pengeluaran_value)

        setupNavigation()
    }

    override fun onStart() {
        super.onStart()
        listenToDashboardData()
    }

    override fun onStop() {
        super.onStop()
        ordersListener?.remove()
        expensesListener?.remove()
    }

    private fun listenToDashboardData() {
        var totalRevenue = 0.0
        var totalExpenses = 0.0

        ordersListener = db.collection("orders")
            .whereEqualTo("status", "Selesai")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("DashboardAdmin", "Listen error on orders", error)
                    return@addSnapshotListener
                }
                totalRevenue = snapshots?.sumOf { it.getDouble("totalPrice") ?: 0.0 } ?: 0.0
                val orderCount = snapshots?.size() ?: 0
                tvJumlahPesanan.text = orderCount.toString()
                updateNetProfit(totalRevenue, totalExpenses)
            }

        expensesListener = db.collection("pengeluaran")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("DashboardAdmin", "Listen error on expenses", error)
                    return@addSnapshotListener
                }
                totalExpenses = snapshots?.sumOf { it.getDouble("jumlah") ?: 0.0 } ?: 0.0
                tvTotalPengeluaran.text = formatCurrency(totalExpenses)
                updateNetProfit(totalRevenue, totalExpenses)
            }
    }

    private fun updateNetProfit(revenue: Double, expenses: Double) {
        val netProfit = revenue - expenses
        tvLabaBersih.text = formatCurrency(netProfit)
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }

    private fun setupNavigation() {
        // PERBAIKAN: Menggunakan ImageButton, bukan MaterialCardView
        findViewById<ImageButton>(R.id.btn_profile).setOnClickListener {
            startActivity(Intent(this, ProfileAdminActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_pos).setOnClickListener {
            startActivity(Intent(this, KasirActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_laporan).setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_menu).setOnClickListener {
            startActivity(Intent(this, ManajemenMenuActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_pengeluaran).setOnClickListener {
            startActivity(Intent(this, ManajemenPengeluaranActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_riwayat_pesanan).setOnClickListener {
             startActivity(Intent(this, RiwayatPesananActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_user).setOnClickListener {
             startActivity(Intent(this, ManajemenUserActivity::class.java))
        }
    }
}
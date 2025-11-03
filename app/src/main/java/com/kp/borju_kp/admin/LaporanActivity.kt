package com.kp.borju_kp.admin

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.print.PrintManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View // <-- PERBAIKAN: Import yang hilang ditambahkan
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.adapter.LaporanAdapter
import com.kp.borju_kp.admin.adapter.LaporanPrintDocumentAdapter
import com.kp.borju_kp.data.Order
import com.kp.borju_kp.data.Pengeluaran
import com.kp.borju_kp.data.TipeTransaksi
import com.kp.borju_kp.data.Transaksi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LaporanActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvLaporan: RecyclerView
    private lateinit var fabPrint: FloatingActionButton
    private lateinit var laporanAdapter: LaporanAdapter

    private val db = FirebaseFirestore.getInstance()
    private var fullTransactionList = mutableListOf<Transaksi>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan)
        enableEdgeToEdge()

        toolbar = findViewById(R.id.toolbar_laporan)
        rvLaporan = findViewById(R.id.rv_laporan)
        fabPrint = findViewById(R.id.fab_print_pdf)

        setupToolbar()
        setupRecyclerView()
        fetchData()

        fabPrint.setOnClickListener { showPrintOptionsDialog() }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        laporanAdapter = LaporanAdapter(listOf())
        rvLaporan.layoutManager = LinearLayoutManager(this)
        rvLaporan.adapter = laporanAdapter
    }

    private fun fetchData() {
        fullTransactionList.clear()
        var ordersLoaded = false
        var expensesLoaded = false

        val onDataLoaded: () -> Unit = {
            if (ordersLoaded && expensesLoaded) {
                fullTransactionList.sortByDescending { it.tanggal }
                laporanAdapter.updateData(fullTransactionList)
            }
        }

        db.collection("orders").whereEqualTo("status", "Selesai").get().addOnSuccessListener {
            it.forEach { doc ->
                val order = doc.toObject(Order::class.java)
                order.orderTimestamp?.let { date ->
                    fullTransactionList.add(Transaksi(doc.id, "Penjualan a/n ${order.customerName}", order.totalPrice, date, TipeTransaksi.PEMASUKAN))
                }
            }
            ordersLoaded = true
            onDataLoaded()
        }.addOnFailureListener { Log.e("LaporanActivity", "Error fetching orders", it) }

        db.collection("pengeluaran").get().addOnSuccessListener {
            it.forEach { doc ->
                val expense = doc.toObject(Pengeluaran::class.java)
                expense.tanggal?.let { date ->
                    fullTransactionList.add(Transaksi(doc.id, expense.namaPengeluaran, expense.jumlah, date, TipeTransaksi.PENGELUARAN))
                }
            }
            expensesLoaded = true
            onDataLoaded()
        }.addOnFailureListener { Log.e("LaporanActivity", "Error fetching expenses", it) }
    }

    private fun showPrintOptionsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_print_options, null)
        val dialog = MaterialAlertDialogBuilder(this).setView(dialogView).create()

        val rgTimeRange = dialogView.findViewById<RadioGroup>(R.id.rg_time_range)
        val datePickersLayout = dialogView.findViewById<LinearLayout>(R.id.date_pickers_layout)
        val btnStartDate = dialogView.findViewById<Button>(R.id.btn_dialog_start_date)
        val btnEndDate = dialogView.findViewById<Button>(R.id.btn_dialog_end_date)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinner_dialog_type)

        var startDate: Calendar? = null
        var endDate: Calendar? = null

        ArrayAdapter.createFromResource(this, R.array.transaction_types, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerType.adapter = adapter
        }

        rgTimeRange.setOnCheckedChangeListener { _, checkedId ->
            datePickersLayout.visibility = if (checkedId == R.id.rb_date_range) View.VISIBLE else View.GONE
        }

        btnStartDate.setOnClickListener { 
            showDatePickerDialog { calendar ->
                startDate = calendar
                btnStartDate.text = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(calendar.time)
            }
        }
        btnEndDate.setOnClickListener { 
            showDatePickerDialog { calendar ->
                endDate = calendar
                btnEndDate.text = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(calendar.time)
            }
         }

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "EXPORT PDF") { _, _ ->
            var filteredList = fullTransactionList.toList()
            if (rgTimeRange.checkedRadioButtonId == R.id.rb_date_range) {
                startDate?.let { start -> filteredList = filteredList.filter { it.tanggal.after(start.time) || it.tanggal == start.time } }
                endDate?.let { end ->
                    val endOfDay = (end.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }
                    filteredList = filteredList.filter { it.tanggal.before(endOfDay.time) }
                }
            }
            val selectedType = spinnerType.selectedItem.toString()
            if (selectedType != "Semua") {
                val tipe = if (selectedType == "Pemasukan") TipeTransaksi.PEMASUKAN else TipeTransaksi.PENGELUARAN
                filteredList = filteredList.filter { it.tipe == tipe }
            }
            printToPdf(filteredList)
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Batal") { d, _ -> d.dismiss() }

        dialog.show()
    }

    private fun showDatePickerDialog(onDateSet: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            onDateSet(selectedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun printToPdf(transaksiList: List<Transaksi>) {
        if(transaksiList.isEmpty()){
            Toast.makeText(this, "Tidak ada data untuk dicetak", Toast.LENGTH_SHORT).show()
            return
        }
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Laporan_Keuangan_Borju_${System.currentTimeMillis()}"
        printManager.print(jobName, LaporanPrintDocumentAdapter(this, transaksiList), null)
    }
}
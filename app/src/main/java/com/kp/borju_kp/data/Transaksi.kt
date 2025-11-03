package com.kp.borju_kp.data

import java.util.Date

/**
 * Data class terpadu untuk merepresentasikan satu baris transaksi,
 * baik itu Pemasukan (dari Order) maupun Pengeluaran.
 */
data class Transaksi(
    val id: String, // ID dari dokumen asli (order atau pengeluaran)
    val deskripsi: String,
    val jumlah: Double,
    val tanggal: Date,
    val tipe: TipeTransaksi // Enum untuk membedakan tipe
)

enum class TipeTransaksi {
    PEMASUKAN,
    PENGELUARAN
}

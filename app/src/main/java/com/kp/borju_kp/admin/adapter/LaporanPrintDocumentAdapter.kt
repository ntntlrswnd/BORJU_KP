package com.kp.borju_kp.admin.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import com.kp.borju_kp.data.TipeTransaksi
import com.kp.borju_kp.data.Transaksi
import java.io.FileOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class LaporanPrintDocumentAdapter(
    private val context: Context,
    private val transaksiList: List<Transaksi>
) : PrintDocumentAdapter() {

    private var document: PdfDocument? = null

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        document = PdfDocument()

        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder("laporan_keuangan.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1) // Implementasi sederhana, 1 halaman
            .build()

        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        // Hanya tulis halaman pertama untuk implementasi sederhana ini
        writePage(0)

        try {
            document?.writeTo(FileOutputStream(destination.fileDescriptor))
        } catch (e: IOException) {
            callback.onWriteFailed(e.toString())
            return
        } finally {
            document?.close()
            document = null
        }

        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
    }

    private fun writePage(pageNumber: Int) {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create() // A4
        val page = document!!.startPage(pageInfo)
        val canvas = page.canvas

        // Definisikan Paint (kuas gambar)
        val titlePaint = Paint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
        val headerPaint = Paint().apply { color = Color.BLACK; textSize = 10f; isFakeBoldText = true }
        val textPaint = Paint().apply { color = Color.BLACK; textSize = 9f }
        val linePaint = Paint().apply { color = Color.BLACK; strokeWidth = 1f }
        val incomePaint = Paint().apply { color = Color.parseColor("#4CAF50"); textSize = 9f }
        val expensePaint = Paint().apply { color = Color.parseColor("#F44336"); textSize = 9f }

        // Definisikan Posisi & Margin
        var yPosition = 40f
        val leftMargin = 40f
        val rightMargin = page.canvas.width - 40f
        val colDate = leftMargin + 5
        val colDesc = leftMargin + 70
        val colIncome = leftMargin + 350
        val colExpense = leftMargin + 450

        // --- MULAI MENGGAMBAR ---

        // 1. Judul Laporan
        canvas.drawText("Laporan Keuangan - Kedai Borju", leftMargin, yPosition, titlePaint)
        yPosition += 30f

        // 2. Header Tabel
        canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, linePaint)
        yPosition += 15f
        canvas.drawText("Tanggal", colDate, yPosition, headerPaint)
        canvas.drawText("Deskripsi", colDesc, yPosition, headerPaint)
        canvas.drawText("Pemasukan", colIncome, yPosition, headerPaint)
        canvas.drawText("Pengeluaran", colExpense, yPosition, headerPaint)
        yPosition += 8f
        canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, linePaint)
        yPosition += 15f

        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        // 3. Isi Tabel (Data Transaksi)
        transaksiList.forEach {
            if (yPosition > 800) return@forEach // Batasi agar tidak overload halaman

            canvas.drawText(sdf.format(it.tanggal), colDate, yPosition, textPaint)
            canvas.drawText(it.deskripsi, colDesc, yPosition, textPaint)

            if (it.tipe == TipeTransaksi.PEMASUKAN) {
                canvas.drawText(formatCurrency(it.jumlah), colIncome, yPosition, incomePaint)
            } else {
                canvas.drawText(formatCurrency(it.jumlah), colExpense, yPosition, expensePaint)
            }
            yPosition += 15f
        }

        // 4. Garis Penutup Tabel
        canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, linePaint)
        yPosition += 20f

        // 5. Ringkasan Total
        val totalPemasukan = transaksiList.filter { it.tipe == TipeTransaksi.PEMASUKAN }.sumOf { it.jumlah }
        val totalPengeluaran = transaksiList.filter { it.tipe == TipeTransaksi.PENGELUARAN }.sumOf { it.jumlah }
        val saldoAkhir = totalPemasukan - totalPengeluaran

        canvas.drawText("Total Pemasukan:", colDesc + 150, yPosition, headerPaint)
        canvas.drawText(formatCurrency(totalPemasukan), colIncome, yPosition, incomePaint)
        yPosition += 15f
        canvas.drawText("Total Pengeluaran:", colDesc + 150, yPosition, headerPaint)
        canvas.drawText(formatCurrency(totalPengeluaran), colIncome, yPosition, expensePaint)
        yPosition += 15f
        canvas.drawText("Saldo Akhir:", colDesc + 150, yPosition, headerPaint)
        canvas.drawText(formatCurrency(saldoAkhir), colIncome, yPosition, headerPaint)
        
        // --- SELESAI MENGGAMBAR ---
        document!!.finishPage(page)
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }
}
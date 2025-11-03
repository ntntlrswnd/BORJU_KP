package com.kp.borju_kp.admin.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Order
import java.text.SimpleDateFormat
import java.util.Locale

class RiwayatPesananAdapter(
    private var orderList: List<Order>,
    private val onCardClick: (String) -> Unit, // Listener untuk klik kartu -> ke detail
    private val onStatusClick: (Order) -> Unit    // Listener untuk klik status -> ubah status
) : RecyclerView.Adapter<RiwayatPesananAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pesanan, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orderList[position])
    }

    override fun getItemCount(): Int = orderList.size

    fun updateData(newOrderList: List<Order>) {
        this.orderList = newOrderList
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val customerName: TextView = itemView.findViewById(R.id.tv_order_customer_name)
        private val totalPrice: TextView = itemView.findViewById(R.id.tv_order_total_price)
        private val timestamp: TextView = itemView.findViewById(R.id.tv_order_timestamp)
        private val orderType: TextView = itemView.findViewById(R.id.tv_order_type)
        private val statusChip: Chip = itemView.findViewById(R.id.chip_order_status)

        fun bind(order: Order) {
            customerName.text = "a/n ${order.customerName}"
            totalPrice.text = "Rp ${order.totalPrice.toInt()}"

            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            timestamp.text = order.orderTimestamp?.let { sdf.format(it) } ?: "-"

            // Tipe Pesanan
            orderType.text = order.orderType
            val background = orderType.background as GradientDrawable
            if (order.orderType.equals("Online", ignoreCase = true)) {
                background.setColor(Color.parseColor("#FF9800")) // Oranye
            } else {
                background.setColor(Color.parseColor("#4CAF50")) // Hijau
            }

            // Status Pesanan
            statusChip.text = order.status
            statusChip.setChipBackgroundColorResource(
                when (order.status) {
                    "Selesai" -> R.color.Tertiary
                    "Ditolak" -> R.color.TextSecondary
                    "Dibatalkan" -> R.color.Error
                    else -> R.color.Primary
                }
            )

            // Listeners
            itemView.setOnClickListener {
                onCardClick(orderList[bindingAdapterPosition].id)
            }
            statusChip.setOnClickListener {
                onStatusClick(orderList[bindingAdapterPosition])
            }
        }
    }
}
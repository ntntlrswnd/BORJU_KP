package com.kp.borju_kp.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kp.borju_kp.R
import com.kp.borju_kp.data.OrderItem

class DetailPesananAdapter(
    private val items: List<OrderItem>
) : RecyclerView.Adapter<DetailPesananAdapter.DetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pesanan_detail, parent, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val quantity: TextView = itemView.findViewById(R.id.tv_detail_item_quantity)
        private val name: TextView = itemView.findViewById(R.id.tv_detail_item_name)
        private val price: TextView = itemView.findViewById(R.id.tv_detail_item_price)
        private val note: TextView = itemView.findViewById(R.id.tv_detail_item_note)

        fun bind(item: OrderItem) {
            quantity.text = "${item.quantity}x"
            name.text = item.menuName
            price.text = "Rp ${(item.price * item.quantity).toInt()}"

            if (item.note.isNotEmpty()) {
                note.visibility = View.VISIBLE
                note.text = "Catatan: ${item.note}"
            } else {
                note.visibility = View.GONE
            }
        }
    }
}
package com.kp.borju_kp.admin.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Menu

class MenuKasirAdapter(
    private var menuList: List<Menu>,
    private val onAddItemClick: (Menu) -> Unit,
    private val onItemClick: (Menu) -> Unit
) : RecyclerView.Adapter<MenuKasirAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu_kasir, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menuList[position]
        holder.bind(menu, onAddItemClick, onItemClick)
    }

    override fun getItemCount(): Int = menuList.size

    fun updateList(newList: List<Menu>) {
        menuList = newList
        notifyDataSetChanged()
    }

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val menuName: TextView = itemView.findViewById(R.id.tv_menu_name)
        private val menuPrice: TextView = itemView.findViewById(R.id.tv_menu_price)
        private val menuImage: ImageView = itemView.findViewById(R.id.iv_menu_image)
        private val addButton: Button = itemView.findViewById(R.id.btn_add_to_cart)
        private val cardView: CardView = itemView.findViewById(R.id.card_menu_item)

        fun bind(menu: Menu, onAddItemClick: (Menu) -> Unit, onItemClick: (Menu) -> Unit) {
            menuName.text = menu.name
            menuPrice.text = "Rp ${menu.price.toInt()}"

            // Load image
            if (menu.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(menu.imageUrl)
                    .centerCrop()
                    .into(menuImage)
            }

            // Stock and Status Logic
            if (!menu.status || menu.stok <= 0) {
                addButton.text = "Habis"
                addButton.isEnabled = false
                // Tambahan: buat tampilan lebih jelas kalau habis
                (itemView as CardView).setCardBackgroundColor(Color.parseColor("#F5F5F5"))
                menuName.setTextColor(Color.GRAY)
            } else {
                addButton.text = "Tambah"
                addButton.isEnabled = true
                // Kembalikan ke warna normal jika stok ada
                (itemView as CardView).setCardBackgroundColor(Color.WHITE)
                menuName.setTextColor(Color.BLACK)
            }

            // Listener untuk tombol Tambah
            addButton.setOnClickListener {
                onAddItemClick(menu)
            }

            // Listener untuk seluruh kartu
            cardView.setOnClickListener {
                onItemClick(menu)
            }
        }
    }
}
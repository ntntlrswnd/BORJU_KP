package com.kp.borju_kp.admin

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.CartManager // <-- PERBAIKAN: Menambahkan import
import com.kp.borju_kp.data.Menu

class DetailMenuActivity : AppCompatActivity() {

    private lateinit var tvMenuName: TextView
    private lateinit var tvMenuPrice: TextView
    private lateinit var tvMenuDescription: TextView
    private lateinit var ivMenuImage: ImageView
    private lateinit var btnAddToCart: Button
    private lateinit var toolbar: MaterialToolbar

    private val db = FirebaseFirestore.getInstance()
    private var currentMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_menu)
        enableEdgeToEdge()

        // Inisialisasi Views
        tvMenuName = findViewById(R.id.tv_detail_menu_name)
        tvMenuPrice = findViewById(R.id.tv_detail_menu_price)
        tvMenuDescription = findViewById(R.id.tv_detail_menu_description)
        ivMenuImage = findViewById(R.id.iv_detail_menu_image)
        btnAddToCart = findViewById(R.id.btn_add_to_cart_detail)
        toolbar = findViewById(R.id.toolbar_detail)

        // Setup Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Ambil ID menu dari Intent
        val menuId = intent.getStringExtra("MENU_ID")

        if (menuId == null) {
            Toast.makeText(this, "Error: Menu tidak ditemukan", Toast.LENGTH_LONG).show()
            finish() // Tutup activity jika tidak ada ID
            return
        }

        fetchMenuDetails(menuId)

        btnAddToCart.setOnClickListener {
            currentMenu?.let {
                CartManager.addItem(it)
                Toast.makeText(this, "${it.name} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke KasirActivity setelah menambahkan
            }
        }
    }

    private fun fetchMenuDetails(menuId: String) {
        db.collection("menus").document(menuId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val menu = document.toObject(Menu::class.java)
                    if (menu != null) {
                        menu.id = document.id // Jangan lupa set ID
                        currentMenu = menu
                        displayMenuDetails(menu)
                    }
                } else {
                    Toast.makeText(this, "Menu tidak ditemukan.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DetailMenuActivity", "Error fetching menu details", exception)
                Toast.makeText(this, "Gagal memuat detail menu.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayMenuDetails(menu: Menu) {
        supportActionBar?.title = menu.name
        tvMenuName.text = menu.name
        tvMenuPrice.text = "Rp ${menu.price.toInt()}"
        tvMenuDescription.text = menu.description ?: "Tidak ada deskripsi."

        // PERBAIKAN: Memuat gambar dari URL menggunakan Glide
        Glide.with(this)
            .load(menu.imageUrl)
            .placeholder(R.drawable.ic_launcher_background) // Gambar sementara saat loading
            .error(R.drawable.ic_launcher_background) // Gambar jika terjadi error
            .into(ivMenuImage)
    }
}

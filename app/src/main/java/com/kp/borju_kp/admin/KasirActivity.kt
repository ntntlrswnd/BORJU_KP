package com.kp.borju_kp.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.adapter.MenuKasirAdapter
import com.kp.borju_kp.data.Menu

class KasirActivity : AppCompatActivity() {

    private lateinit var rvMenuKasir: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var tvItemCount: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnCheckout: Button

    private lateinit var menuAdapter: MenuKasirAdapter
    private val db = FirebaseFirestore.getInstance()
    private var menuListener: ListenerRegistration? = null
    private var fullMenuList = listOf<Menu>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kasir)
        enableEdgeToEdge()

        // Setup Toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inisialisasi Views
        rvMenuKasir = findViewById(R.id.rv_menu_kasir)
        tabLayout = findViewById(R.id.tab_layout_kategori)
        tvItemCount = findViewById(R.id.tv_item_count)
        tvTotalPrice = findViewById(R.id.tv_total_price)
        btnCheckout = findViewById(R.id.btn_checkout)

        setupRecyclerView()
        setupTabs()

        // Listener untuk tombol Checkout
        btnCheckout.setOnClickListener {
            if (CartManager.getCartItems().isNotEmpty()) {
                startActivity(Intent(this, CheckoutActivity::class.java))
            } else {
                Toast.makeText(this, "Keranjang masih kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        listenToMenuData()
    }

    override fun onStop() {
        super.onStop()
        menuListener?.remove()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        updateCartSummary()
    }

    private fun setupRecyclerView() {
        rvMenuKasir.layoutManager = LinearLayoutManager(this)
        menuAdapter = MenuKasirAdapter(
            menuList = listOf(),
            onAddItemClick = { menu ->
                CartManager.addItem(menu)
                updateCartSummary()
                Toast.makeText(this, "${menu.name} ditambahkan", Toast.LENGTH_SHORT).show()
            },
            onItemClick = { menu ->
                val intent = Intent(this, DetailMenuActivity::class.java)
                intent.putExtra("MENU_ID", menu.id)
                startActivity(intent)
            }
        )
        rvMenuKasir.adapter = menuAdapter
    }

    private fun setupTabs() {
        val categories = listOf("Semua", "Makanan", "Minuman", "Snack", "Kopi")
        categories.forEach { category ->
            tabLayout.addTab(tabLayout.newTab().setText(category))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterMenuList(tab?.text.toString())
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) { /* No-op */ }
            override fun onTabReselected(tab: TabLayout.Tab?) { /* No-op */ }
        })
    }

    private fun listenToMenuData() {
        menuListener = db.collection("menus")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { result, error ->
                if (error != null) {
                    Log.e("KasirActivity", "Error listening to menu data", error)
                    return@addSnapshotListener
                }
                if (result != null) {
                    val menuList = result.map {
                        val menu = it.toObject(Menu::class.java)
                        menu.id = it.id
                        menu
                    }
                    fullMenuList = menuList
                    val selectedTab = tabLayout.getTabAt(tabLayout.selectedTabPosition)
                    filterMenuList(selectedTab?.text.toString())
                }
            }
    }

    private fun filterMenuList(category: String) {
        val filteredList = if (category.equals("Semua", ignoreCase = true) || category.isEmpty()) {
            fullMenuList
        } else {
            fullMenuList.filter { it.kategori.equals(category, ignoreCase = true) }
        }
        menuAdapter.updateList(filteredList)
    }

    private fun updateCartSummary() {
        val itemCount = CartManager.getTotalItemCount()
        val totalPrice = CartManager.getTotalPrice()

        tvItemCount.text = "$itemCount Item"
        tvTotalPrice.text = "Rp ${totalPrice.toInt()}"
    }
}
package com.kp.borju_kp.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


data class Order(
    @get:Exclude var id: String = "", // <-- DITAMBAHKAN: Untuk menyimpan ID dokumen
    val customerName: String = "",
    val paymentMethod: String = "",
    val totalPrice: Double = 0.0,
    val status: String = "Baru",
    val orderType: String = "Offline",
    @ServerTimestamp
    val orderTimestamp: Date? = null,
    val items: List<OrderItem> = listOf()
)

data class OrderItem(
    val menuId: String = "",
    val menuName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val note: String = ""
)


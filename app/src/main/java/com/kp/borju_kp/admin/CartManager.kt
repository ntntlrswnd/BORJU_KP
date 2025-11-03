package com.kp.borju_kp.admin

import com.kp.borju_kp.data.Menu

data class CartItem(
    val menu: Menu,
    var quantity: Int = 1,
    var note: String = ""
)

object CartManager {

    private val cartItems = mutableListOf<CartItem>()

    // Fungsi ini tetap ada untuk menambah item dari halaman kasir
    fun addItem(menu: Menu) {
        val existingItem = cartItems.find { it.menu.id == menu.id }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(CartItem(menu = menu, quantity = 1, note = ""))
        }
    }

    // FUNGSI BARU: Menambah kuantitas item yang sudah ada
    fun increaseItemQuantity(cartItem: CartItem) {
        cartItem.quantity++
    }

    // FUNGSI BARU: Mengurangi kuantitas. Jika jadi 0, item akan dihapus.
    fun decreaseItemQuantity(cartItem: CartItem) {
        if (cartItem.quantity > 1) {
            cartItem.quantity--
        } else {
            removeItem(cartItem) // Hapus jika kuantitas tinggal 1
        }
    }

    // FUNGSI BARU: Menghapus item dari keranjang
    fun removeItem(cartItem: CartItem) {
        cartItems.remove(cartItem)
    }

    fun getCartItems(): List<CartItem> {
        return cartItems
    }

    fun getTotalItemCount(): Int {
        return cartItems.sumOf { it.quantity }
    }

    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.menu.price * it.quantity }
    }

    fun clearCart() {
        cartItems.clear()
    }
}

package com.kp.borju_kp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.kp.borju_kp.admin.DashboardAdmin
import com.kp.borju_kp.auth.LoginActivity
import com.kp.borju_kp.customer.DashboardCostumer
import com.kp.borju_kp.utils.SessionManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek apakah user sudah login melalui Firebase Auth
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null && SessionManager.isLoggedIn()) {
            // Jika user sudah login, cek role-nya dari session
            when (SessionManager.getUserRole()) {
                "Admin" -> {
                    startActivity(Intent(this, DashboardAdmin::class.java))
                }
                "Customer" -> {
                    startActivity(Intent(this, DashboardCostumer::class.java))
                }
                else -> {
                    // Jika role tidak ada/aneh, arahkan ke login
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        } else {
            // Jika belum login, arahkan ke halaman Login
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Tutup MainActivity agar tidak bisa kembali ke halaman ini
        finish()
    }
}
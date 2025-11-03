package com.kp.borju_kp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.DashboardAdmin
import com.kp.borju_kp.customer.DashboardCostumer
import com.kp.borju_kp.data.User
import com.kp.borju_kp.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvGoRegister = findViewById<TextView>(R.id.tv_go_register)
        val tvResetPassword = findViewById<TextView>(R.id.tv_reset_password)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { checkUserRoleAndProceed(it.uid) }
                } else {
                    Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvGoRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        tvResetPassword.setOnClickListener { startActivity(Intent(this, ResetPasswordActivity::class.java)) }
    }

    private fun checkUserRoleAndProceed(userId: String) {
        db.collection("USER").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                if (user != null) {
                    // SIMPAN SESI DI SINI
                    SessionManager.saveSession(user)

                    // Arahkan ke dashboard yang sesuai
                    when (user.nama_role) {
                        "Admin" -> {
                            Toast.makeText(this, "Login sebagai Admin", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, DashboardAdmin::class.java))
                            finishAffinity() // Tutup semua activity sebelumnya
                        }
                        "Customer" -> {
                            Toast.makeText(this, "Login sebagai Customer", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, DashboardCostumer::class.java))
                            finishAffinity() // Tutup semua activity sebelumnya
                        }
                        else -> Toast.makeText(this, "Role tidak dikenali!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Gagal memproses data user!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Data user tidak ditemukan!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Gagal ambil data user: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
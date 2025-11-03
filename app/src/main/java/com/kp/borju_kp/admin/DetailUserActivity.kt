package com.kp.borju_kp.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.data.User

class DetailUserActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var actvRole: AutoCompleteTextView
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button

    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_user)
        enableEdgeToEdge()

        toolbar = findViewById(R.id.toolbar_detail_user)
        etName = findViewById(R.id.et_detail_user_name)
        etEmail = findViewById(R.id.et_detail_user_email)
        actvRole = findViewById(R.id.actv_detail_user_role)
        btnUpdate = findViewById(R.id.btn_update_user)
        btnDelete = findViewById(R.id.btn_delete_user)

        setupToolbar()
        setupRoleDropdown()

        userId = intent.getStringExtra("USER_ID")
        if (userId == null) {
            showErrorAndFinish("ID Pengguna tidak valid")
            return
        }

        fetchUserDetails(userId!!)

        btnUpdate.setOnClickListener { updateUserData() }
        btnDelete.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun updateUserData() {
        val newName = etName.text.toString().trim()
        val newRole = actvRole.text.toString()

        if (newName.isEmpty()) {
            etName.error = "Nama tidak boleh kosong"
            return
        }
        
        val newRoleId = if (newRole.equals("Admin", ignoreCase = true)) "1" else "2"

        val updatedData = mapOf(
            "nama_user" to newName,
            "nama_role" to newRole,
            "id_role" to newRoleId
        )

        userId?.let {
            db.collection("USER").document(it).update(updatedData).addOnSuccessListener {
                Toast.makeText(this, "Data pengguna berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Pengguna")
            .setMessage("Anda yakin ingin menghapus pengguna ini? Tindakan ini tidak dapat dibatalkan.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ -> deleteUser() }
            .show()
    }

    private fun deleteUser() {
        userId?.let {
            db.collection("USER").document(it).delete().addOnSuccessListener {
                Toast.makeText(this, "Pengguna berhasil dihapus", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserDetails(id: String) {
        db.collection("USER").document(id).get().addOnSuccessListener {
            val user = it.toObject(User::class.java)
            if (user != null) {
                displayUserDetails(user)
            } else {
                showErrorAndFinish("Gagal membaca data pengguna")
            }
        }.addOnFailureListener { showErrorAndFinish("Gagal mengambil data pengguna") }
    }

    private fun displayUserDetails(user: User) {
        etName.setText(user.nama_user)
        etEmail.setText(user.email)
        actvRole.setText(user.nama_role, false)
    }

    private fun setupRoleDropdown() {
        val roles = arrayOf("Admin", "Customer")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        actvRole.setAdapter(adapter)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun showErrorAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
}
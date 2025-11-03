package com.kp.borju_kp.admin

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.data.User

class EditProfileAdminActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivProfile: ImageView
    private lateinit var btnChangePhoto: Button
    private lateinit var etName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var btnSave: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var imageUri: Uri? = null
    private var currentImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_admin)
        enableEdgeToEdge()

        toolbar = findViewById(R.id.toolbar_edit_profile)
        ivProfile = findViewById(R.id.iv_edit_profile_image)
        btnChangePhoto = findViewById(R.id.btn_change_photo)
        etName = findViewById(R.id.et_edit_profile_name)
        etPhone = findViewById(R.id.et_edit_profile_phone)
        etAddress = findViewById(R.id.et_edit_profile_address)
        btnSave = findViewById(R.id.btn_save_profile)

        setupToolbar()
        loadInitialData()

        btnChangePhoto.setOnClickListener { pickImageFromGallery() }
        btnSave.setOnClickListener { saveProfileChanges() }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            imageUri = it.data?.data
            Glide.with(this).load(imageUri).circleCrop().into(ivProfile)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun loadInitialData() {
        auth.currentUser?.uid?.let {
            db.collection("USER").document(it).get().addOnSuccessListener {
                val user = it.toObject(User::class.java)
                if (user != null) {
                    etName.setText(user.nama_user)
                    etPhone.setText(user.nohp)
                    etAddress.setText(user.alamat)
                    currentImageUrl = user.profile_image_url
                    if (!currentImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(currentImageUrl).circleCrop().into(ivProfile)
                    }
                }
            }
        }
    }

    private fun saveProfileChanges() {
        val newName = etName.text.toString().trim()
        val newPhone = etPhone.text.toString().trim()
        val newAddress = etAddress.text.toString().trim()

        if (newName.isEmpty()) {
            etName.error = "Nama tidak boleh kosong"
            return
        }

        val progressDialog = ProgressDialog(this).apply { setMessage("Menyimpan..."); setCancelable(false); show() }

        if (imageUri != null) {
            uploadToCloudinaryAndSave(progressDialog, newName, newPhone, newAddress)
        } else {
            saveDataToFirestore(progressDialog, currentImageUrl ?: "", newName, newPhone, newAddress)
        }
    }

    private fun uploadToCloudinaryAndSave(dialog: ProgressDialog, name: String, phone: String, address: String) {
        imageUri?.let {
            MediaManager.get().upload(it).callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    dialog.setMessage("Mengunggah foto...")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val newImageUrl = resultData?.get("secure_url") as? String ?: ""
                    saveDataToFirestore(dialog, newImageUrl, name, phone, address)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    dialog.dismiss()
                    Toast.makeText(baseContext, "Gagal upload foto: ${error?.description}", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
        }
    }

    private fun saveDataToFirestore(dialog: ProgressDialog, imageUrl: String, name: String, phone: String, address: String) {
        val userId = auth.currentUser?.uid ?: return

        val updatedData = mapOf(
            "nama_user" to name,
            "nohp" to phone,
            "alamat" to address,
            "profile_image_url" to imageUrl
        )

        db.collection("USER").document(userId).update(updatedData).addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            dialog.dismiss()
            Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}
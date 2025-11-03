package com.kp.borju_kp.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.adapter.UserAdapter
import com.kp.borju_kp.auth.RegisterActivity
import com.kp.borju_kp.data.User

class ManajemenUserActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var fabAddUser: FloatingActionButton

    private val db = FirebaseFirestore.getInstance()
    private var usersListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manajemen_user)
        enableEdgeToEdge()

        toolbar = findViewById(R.id.toolbar_manajemen_user)
        rvUsers = findViewById(R.id.rv_users)
        progressBar = findViewById(R.id.progress_bar_users)
        tvEmpty = findViewById(R.id.tv_empty_users)
        fabAddUser = findViewById(R.id.fab_add_user)

        setupToolbar()
        setupRecyclerView()

        fabAddUser.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        listenToUsers()
    }

    override fun onStop() {
        super.onStop()
        usersListener?.remove()
    }

    private fun listenToUsers() {
        progressBar.visibility = View.VISIBLE
        rvUsers.visibility = View.GONE
        tvEmpty.visibility = View.GONE

        usersListener = db.collection("USER").addSnapshotListener { snapshots, error ->
            progressBar.visibility = View.GONE
            if (error != null) {
                Log.e("ManajemenUser", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                val userList = snapshots.documents.map {
                    val user = it.toObject(User::class.java)
                    user?.uid = it.id
                    user!!
                }
                userAdapter.updateData(userList)
                rvUsers.visibility = View.VISIBLE
            } else {
                tvEmpty.visibility = View.VISIBLE
            }
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(listOf()) { userId ->
            val intent = Intent(this, DetailUserActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = userAdapter
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}

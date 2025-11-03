package com.kp.borju_kp.admin.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kp.borju_kp.R
import com.kp.borju_kp.data.User

class UserAdapter(
    private var userList: List<User>,
    private val onItemClick: (String) -> Unit // Mengirim UID Pengguna
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = userList.size

    fun updateData(newUserList: List<User>) {
        this.userList = newUserList
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tv_user_name)
        private val email: TextView = itemView.findViewById(R.id.tv_user_email)
        private val role: TextView = itemView.findViewById(R.id.tv_user_role)

        fun bind(user: User) {
            name.text = user.nama_user
            email.text = user.email
            role.text = user.nama_role.replaceFirstChar { it.uppercase() }

            val background = role.background as GradientDrawable
            if (user.nama_role.equals("Admin", ignoreCase = true)) {
                background.setColor(Color.parseColor("#F44336")) // Merah untuk Admin
            } else {
                background.setColor(Color.parseColor("#2196F3")) // Biru untuk Customer
            }

            itemView.setOnClickListener {
                val userId = userList[bindingAdapterPosition].uid
                onItemClick(userId)
            }
        }
    }
}
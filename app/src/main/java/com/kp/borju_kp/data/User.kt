package com.kp.borju_kp.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class User(
    @get:Exclude var uid: String = "",

    @get:PropertyName("nama_user") @set:PropertyName("nama_user") var nama_user: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("nama_role") @set:PropertyName("nama_role") var nama_role: String = "Customer",
    @get:PropertyName("nohp") @set:PropertyName("nohp") var nohp: String = "",
    @get:PropertyName("alamat") @set:PropertyName("alamat") var alamat: String = "",
    @get:PropertyName("id_user") @set:PropertyName("id_user") var id_user: String = "",
    @get:PropertyName("id_role") @set:PropertyName("id_role") var id_role: String = "",
    @get:PropertyName("profile_image_url") @set:PropertyName("profile_image_url") var profile_image_url: String = "" // <-- DITAMBAHKAN
)

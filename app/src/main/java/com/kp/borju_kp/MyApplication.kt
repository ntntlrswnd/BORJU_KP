package com.kp.borju_kp

import android.app.Application
import com.kp.borju_kp.utils.SessionManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inisialisasi SessionManager di sini
        SessionManager.init(this)
    }
}
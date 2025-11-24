package com.example.persona

import android.app.Application
import com.example.persona.data.UserPreferences

class MyApplication : Application() {

    companion object {
        // 全局静态单例，方便直接访问
        lateinit var prefs: UserPreferences
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化 DataStore
        prefs = UserPreferences(this)
    }
}
package com.example.persona

import android.app.Application
import android.content.Context
import com.example.persona.data.UserPreferences

class MyApplication : Application() {

    companion object {
        // 全局静态单例，方便直接访问
        lateinit var prefs: UserPreferences
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化 DataStore
        prefs = UserPreferences(this)
        instance = this
    }
}
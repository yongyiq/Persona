package com.example.persona.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// 1. 扩展属性：创建一个名为 "user_prefs" 的 DataStore 文件
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        // 定义存储的 Key
        val KEY_USER_ID = longPreferencesKey("user_id")
        val KEY_ACTIVE_PERSONA_ID = stringPreferencesKey("active_persona_id")
    }

    // --- A. User ID (当前登录用户) ---
    
    // 获取 (Flow 形式，用于实时监听)
    val userIdFlow: Flow<Long?> = context.dataStore.data
        .map { preferences -> preferences[KEY_USER_ID] }

    // 获取 (一次性，挂起函数，方便在 ViewModel 里直接用)
    suspend fun getUserId(): Long {
        // 如果没存过，默认返回 1L (应对毕设演示)
        return userIdFlow.first() ?: 0L
    }

    // 保存
    suspend fun saveUserId(id: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = id
        }
    }

    // --- B. Active Persona ID (当前扮演的角色) ---

    val activePersonaIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[KEY_ACTIVE_PERSONA_ID] }

    suspend fun getActivePersonaId(): String? {
        return activePersonaIdFlow.first()
    }

    suspend fun saveActivePersonaId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_PERSONA_ID] = id
        }
    }
    // 退出登录：清除所有数据
    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
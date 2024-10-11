package com.sarahang.playback.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PreferencesStore @Inject constructor(private val dataStore: DataStore<Preferences>) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun <T> save(keyName: String, value: T, serializer: KSerializer<T>) {
        val key = stringPreferencesKey(keyName)
        save(key, json.encodeToString(serializer, value))
    }

    suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        dataStore.edit { settings ->
            settings[key] = value
        }
    }

    fun <T> data(key: Preferences.Key<T>): Flow<T?> =
        dataStore.data.map { preferences ->
            preferences[key]
        }

    fun <T> get(key: String, serializer: KSerializer<T>, defaultValue: T): Flow<T> {
        return dataStore.data
            .map { preferences ->
                try {
                    json.decodeFromString(
                        serializer,
                        preferences[stringPreferencesKey(key)].orEmpty()
                    )
                } catch (ex: Exception) {
                    defaultValue
                }
            }
    }

    fun <T> get(key: Preferences.Key<T>, defaultValue: T): Flow<T> = dataStore.data
        .map { preferences -> preferences[key] ?: return@map defaultValue }

    fun <T> getStateFlow(
        keyName: Preferences.Key<T>,
        scope: CoroutineScope,
        initialValue: T,
        saveDebounce: Long = 0,
    ): MutableStateFlow<T> {
        val state = MutableStateFlow(initialValue)
        scope.launch {
            state.value = get(keyName, initialValue).first()
            state.debounce(saveDebounce)
                .collectLatest { save(keyName, it) }
        }
        return state
    }
}

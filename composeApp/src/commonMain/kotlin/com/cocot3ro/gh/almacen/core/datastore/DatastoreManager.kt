package com.cocot3ro.gh.almacen.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.cocot3ro.gh.almacen.di.AuthDatastore
import com.cocot3ro.gh.almacen.di.PreferencesDatastore
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class DatastoreManager(
    @Provided @PreferencesDatastore private val preferencesDataStore: DataStore<Preferences>,
    @Provided @AuthDatastore private val authDataStore: DataStore<Preferences>
) {
    fun getPreferences(): Flow<Preferences> = preferencesDataStore.data

    fun getAuthPreferences(): Flow<Preferences> = authDataStore.data

    suspend fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        preferencesDataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun <T> saveAuthPreference(key: Preferences.Key<T>, value: T?) {
        authDataStore.edit { preferences ->
            value?.let {
                preferences[key] = value
            } ?: preferences.remove(key)
        }
    }
}
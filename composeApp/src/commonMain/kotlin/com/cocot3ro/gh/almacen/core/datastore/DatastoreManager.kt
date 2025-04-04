package com.cocot3ro.gh.almacen.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.cocot3ro.gh.almacen.core.datastore.DatastoreConstants.Keys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class DatastoreManager(
    @Provided private val dataStore: DataStore<Preferences>,
) {

    fun getPreferences(): Flow<Map<Preferences.Key<*>, Any?>> {
        return dataStore.data.map {
            mapOf(
                Keys.HOST to it[Keys.HOST],
                Keys.PORT to it[Keys.PORT],
                Keys.FIRST_TIME to it[Keys.FIRST_TIME]
            )
        }
    }

    suspend fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

}
package com.cocot3ro.gh.almacen.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DatastoreConstants {
    internal const val DATA_STORE_FILE_NAME = "prefs.preferences_pb"

    object Keys {
        val FIRST_TIME = booleanPreferencesKey("first_time")

        val HOST = stringPreferencesKey("host")
        val PORT = intPreferencesKey("port")
    }

    object Defaults {
        const val FIRST_TIME = true

        const val HOST = "localhost"
        const val PORT = 8080
    }
}
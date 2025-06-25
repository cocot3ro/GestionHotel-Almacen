package com.cocot3ro.gh.almacen.core.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PrefsDatastoreConstants {
    internal const val PREFS_DATA_STORE_FILE_NAME: String = "prefs.preferences_pb"

    object Keys {
        val HOST: Preferences.Key<String> = stringPreferencesKey("host")
        val PORT: Preferences.Key<Int> = intPreferencesKey("port")
    }

    object Defaults {
        val defaultPreferences: Preferences
            get() = emptyPreferences()
    }
}
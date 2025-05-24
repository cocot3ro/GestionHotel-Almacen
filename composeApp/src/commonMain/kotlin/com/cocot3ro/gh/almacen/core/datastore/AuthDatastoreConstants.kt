package com.cocot3ro.gh.almacen.core.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey

object AuthDatastoreConstants {
    internal const val AUTH_DATA_STORE_FILE_NAME: String = "auth.preferences_pb"

    object Keys {
        val JWT_TOKEN: Preferences.Key<String> = stringPreferencesKey("jwt_token")
        val REFRESH_TOKEN: Preferences.Key<String> = stringPreferencesKey("refresh_token")
    }

    object Defaults {
        val defaultPreferences: Preferences
            get() = emptyPreferences()
    }
}
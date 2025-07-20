package com.cocot3ro.gh.almacen.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.cocot3ro.gh.almacen.di.AuthDatastore
import com.cocot3ro.gh.almacen.di.PreferencesDatastore
import okio.Path.Companion.toOkioPath
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Single
@PreferencesDatastore
actual fun getPreferencesDatastore(scope: Scope): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        corruptionHandler = ReplaceFileCorruptionHandler {
            PrefsDatastoreConstants.Defaults.defaultPreferences
        },
        produceFile = {
            scope.get<Context>().filesDir
                .resolve(PrefsDatastoreConstants.PREFS_DATA_STORE_FILE_NAME)
                .toOkioPath()
        }
    )
}

@Single
@AuthDatastore
actual fun getAuthDatastore(scope: Scope): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        corruptionHandler = ReplaceFileCorruptionHandler {
            AuthDatastoreConstants.Defaults.defaultPreferences
        },
        produceFile = {
            scope.get<Context>().filesDir
                .resolve(AuthDatastoreConstants.AUTH_DATA_STORE_FILE_NAME)
                .toOkioPath()
        }
    )
}
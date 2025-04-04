package com.cocot3ro.gh.almacen.core.datastore

import com.cocot3ro.gh.almacen.core.datastore.DatastoreConstants.Defaults
import com.cocot3ro.gh.almacen.core.datastore.DatastoreConstants.Keys
import com.cocot3ro.gh.almacen.domain.model.PreferenceItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class DatastoreRepository(
    @Provided private val datastoreManager: DatastoreManager,
) {

    fun getPreferences(): Flow<PreferenceItem> {
        return datastoreManager.getPreferences().map { map ->
            PreferenceItem(
                host = (map[Keys.HOST] ?: Defaults.HOST) as String,
                port = (map[Keys.PORT] ?: Defaults.PORT) as Int,
                firstTime = (map[Keys.FIRST_TIME] ?: Defaults.FIRST_TIME) as Boolean
            )
        }
    }

    suspend fun setHost(host: String) {
        datastoreManager.savePreference(Keys.HOST, host)
    }

    suspend fun setPort(port: Int) {
        datastoreManager.savePreference(Keys.PORT, port)
    }

    suspend fun setFirstTime(firstTime: Boolean) {
        datastoreManager.savePreference(Keys.FIRST_TIME, firstTime)
    }

}
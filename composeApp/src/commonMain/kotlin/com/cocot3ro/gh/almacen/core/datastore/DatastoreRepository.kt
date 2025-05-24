package com.cocot3ro.gh.almacen.core.datastore

import com.cocot3ro.gh.almacen.core.datastore.model.AuthPreferenceCore
import com.cocot3ro.gh.almacen.core.datastore.model.PreferenceCore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class DatastoreRepository(
    @Provided private val datastoreManager: DatastoreManager
) {

    fun getPreferences(): Flow<PreferenceCore> {
        return datastoreManager.getPreferences().map { map ->

            val host: String? = map[PrefsDatastoreConstants.Keys.HOST]
            val port: UShort? = map[PrefsDatastoreConstants.Keys.PORT]?.toUShort()

            PreferenceCore(
                host = host,
                port = port
            )
        }
    }

    suspend fun setHost(host: String) {
        datastoreManager.savePreference(PrefsDatastoreConstants.Keys.HOST, host)
    }

    suspend fun setPort(port: UShort) {
        datastoreManager.savePreference(PrefsDatastoreConstants.Keys.PORT, port.toInt())
    }

    fun getAuthPreferences(): Flow<AuthPreferenceCore> =
        datastoreManager.getAuthPreferences().map { map ->
            val jwtToken: String? = map[AuthDatastoreConstants.Keys.JWT_TOKEN]
            val refreshToken: String? = map[AuthDatastoreConstants.Keys.REFRESH_TOKEN]

            AuthPreferenceCore(
                jwtToken = jwtToken,
                refreshToken = refreshToken
            )
        }

    suspend fun setJwtToken(token: String?) {
        datastoreManager.saveAuthPreference(AuthDatastoreConstants.Keys.JWT_TOKEN, token)
    }

    suspend fun setRefreshToken(token: String?) {
        datastoreManager.saveAuthPreference(AuthDatastoreConstants.Keys.REFRESH_TOKEN, token)
    }
}
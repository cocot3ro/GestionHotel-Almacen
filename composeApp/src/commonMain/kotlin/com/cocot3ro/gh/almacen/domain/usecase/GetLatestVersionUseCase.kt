package com.cocot3ro.gh.almacen.domain.usecase

import com.cocot3ro.gh.almacen.data.network.model.AppVersionModel
import com.cocot3ro.gh.almacen.data.network.model.ext.toDomain
import com.cocot3ro.gh.almacen.data.network.repository.NetworkRepository
import com.cocot3ro.gh.almacen.domain.model.AppDistribution
import com.cocot3ro.gh.almacen.domain.model.AppVersionDomain
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class GetLatestVersionUseCase(
    @Provided private val networkRepository: NetworkRepository
) {

    suspend operator fun invoke(appDistribution: AppDistribution): Result<AppVersionDomain> {
        val appVersionModel: Result<AppVersionModel> = networkRepository.getLatestVersion()

        if (appVersionModel.isFailure) {
            return Result.failure(appVersionModel.exceptionOrNull() ?: Exception("Unknown error"))
        }

        return appVersionModel.mapCatching { model ->
            AppVersionDomain(
                version = when (appDistribution) {
                    AppDistribution.DESKTOP -> model.desktop.toDomain()
                    AppDistribution.ANDROID -> model.android.toDomain()
                }
            )
        }
    }
}

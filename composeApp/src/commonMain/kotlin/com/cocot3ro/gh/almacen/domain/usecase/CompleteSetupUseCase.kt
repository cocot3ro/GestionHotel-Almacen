package com.cocot3ro.gh.almacen.domain.usecase

import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class CompleteSetupUseCase(
    @Provided private val savePreferencesUseCase: SavePreferencesUseCase,
) {

    suspend operator fun invoke() {
        savePreferencesUseCase.setFirstTime(firstTime = false)
    }

}
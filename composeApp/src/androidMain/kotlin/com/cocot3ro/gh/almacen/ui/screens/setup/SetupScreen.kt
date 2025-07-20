package com.cocot3ro.gh.almacen.ui.screens.setup

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(
    modifier: Modifier,
    onNavigateToHome: () -> Unit,
    setupStep: SetupStep? = null
) {

    BackHandler(onBack = onNavigateToHome)

    val context: Context = LocalContext.current
    val cameraPermission: Int = context.checkSelfPermission(android.Manifest.permission.CAMERA)

    val pagerState: PagerState = rememberPagerState(
        initialPage = when (setupStep) {
            SetupStep.CAMERA,
            null -> if (cameraPermission == PackageManager.PERMISSION_GRANTED) 1 else 0

            SetupStep.SERVER -> 1
        },
        pageCount = { 2 }
    )
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        userScrollEnabled = false
    ) { page: Int ->
        when (page) {
            0 -> SetupCamera(
                modifier = Modifier.fillMaxSize(),
                onSetupCompleted = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            )

            1 -> SetupServer(
                modifier = Modifier.fillMaxSize(),
                onSetupCompleted = onNavigateToHome
            )
        }
    }
}

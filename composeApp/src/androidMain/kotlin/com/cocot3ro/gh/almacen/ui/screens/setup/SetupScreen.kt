package com.cocot3ro.gh.almacen.ui.screens.setup

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SetupScreen(
    modifier: Modifier,
    @Suppress("UndeclaredKoinUsage")
    viewModel: SetupViewModel = koinViewModel(),
    onSetupCompleted: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 2 }

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        userScrollEnabled = false
    ) { pageIndex ->
        when (pageIndex) {
            0 -> {
                SetupFase1(
                    modifier = Modifier
                        .fillMaxSize(),
                    onSetupFaseCompleted = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
            }

            1 -> {
                SetupFase2(
                    modifier = Modifier
                        .fillMaxSize(),
                    onSetupFaseCompleted = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                viewModel.completeSetUp()
                            }
                            onSetupCompleted()
                        }
                    }
                )
            }
        }
    }
}

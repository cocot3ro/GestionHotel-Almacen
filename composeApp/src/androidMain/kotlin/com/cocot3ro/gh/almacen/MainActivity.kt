package com.cocot3ro.gh.almacen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cocot3ro.gh.almacen.ui.navigation.NavigationWrapper
import com.cocot3ro.gh.almacen.ui.theme.GhAlmacenTheme
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            KoinContext {
                GhAlmacenTheme {
                    NavigationWrapper()
                }
            }
        }
    }
}

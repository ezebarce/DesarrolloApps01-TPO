package com.cuentasclaras.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cuentasclaras.app.core.designsystem.theme.CuentasClarasTheme
import com.cuentasclaras.app.ui.CuentasClarasApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CuentasClarasTheme {
                CuentasClarasApp()
            }
        }
    }
}

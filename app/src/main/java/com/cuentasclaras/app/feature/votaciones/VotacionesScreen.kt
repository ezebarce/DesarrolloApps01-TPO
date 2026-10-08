package com.cuentasclaras.app.feature.votaciones

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cuentasclaras.app.core.designsystem.theme.Typography

@Composable
fun VotacionesScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Votaciones Screen", style = Typography.headlineMedium)
    }
}

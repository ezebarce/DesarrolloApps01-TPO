package com.cuentasclaras.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: Screen,
    val icon: ImageVector,
    val text: String
) {
    INICIO(
        route = Screen.Inicio,
        icon = Icons.Filled.Dashboard,
        text = "Inicio"
    ),
    GASTOS(
        route = Screen.Gastos,
        icon = Icons.Filled.ReceiptLong,
        text = "Gastos"
    ),
    VOTACIONES(
        route = Screen.Votaciones,
        icon = Icons.Filled.HowToVote,
        text = "Votaciones"
    ),
    ALERTAS(
        route = Screen.Alertas,
        icon = Icons.Filled.Notifications,
        text = "Alertas"
    )
}

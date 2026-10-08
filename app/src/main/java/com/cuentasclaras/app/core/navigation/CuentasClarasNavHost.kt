package com.cuentasclaras.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cuentasclaras.app.feature.alertas.AlertasScreen
import com.cuentasclaras.app.feature.gastos.GastosScreen
import com.cuentasclaras.app.feature.inicio.InicioScreen
import com.cuentasclaras.app.feature.votaciones.VotacionesScreen

@Composable
fun CuentasClarasNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Inicio,
        modifier = modifier
    ) {
        composable<Screen.Inicio> {
            InicioScreen()
        }
        composable<Screen.Gastos> {
            GastosScreen()
        }
        composable<Screen.Votaciones> {
            VotacionesScreen()
        }
        composable<Screen.Alertas> {
            AlertasScreen()
        }
    }
}

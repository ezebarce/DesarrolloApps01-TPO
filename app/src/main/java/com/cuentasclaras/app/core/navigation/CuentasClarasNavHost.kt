package com.cuentasclaras.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cuentasclaras.app.feature.alertas.AlertasScreen
import com.cuentasclaras.app.feature.gastos.GastosScreen
import com.cuentasclaras.app.feature.gastos.DetalleGastoScreen
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
            GastosScreen(
                onNavigateToDetalleGasto = { id -> navController.navigate(Screen.DetalleGasto(id)) },
                onNavigateToFormularioConcepto = { id -> navController.navigate(Screen.FormularioConcepto(id)) },
                onNavigateToFormularioGasto = { navController.navigate(Screen.FormularioGasto(null)) }
            )
        }
        composable<Screen.Votaciones> {
            VotacionesScreen(
                onNavigateToDetalleVotacion = { id -> navController.navigate(Screen.DetalleVotacion(id)) },
                onNavigateToFormularioVotacion = { id -> navController.navigate(Screen.FormularioVotacion(id)) }
            )
        }
        composable<Screen.Alertas> {
            AlertasScreen()
        }
        composable<Screen.DetalleGasto> {
            DetalleGastoScreen(
                onBack = { navController.popBackStack() },
                onEditGasto = { id -> navController.navigate(Screen.FormularioGasto(id)) },
                onAddPresupuesto = { gastoId -> navController.navigate(Screen.FormularioPresupuesto(gastoId, null)) },
                onEditPresupuesto = { gastoId, pId -> navController.navigate(Screen.FormularioPresupuesto(gastoId, pId)) }
            )
        }
        composable<Screen.FormularioGasto> {
            com.cuentasclaras.app.feature.gastos.FormularioGastoScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<Screen.FormularioConcepto> {
            com.cuentasclaras.app.feature.gastos.FormularioConceptoScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<Screen.FormularioPresupuesto> {
            com.cuentasclaras.app.feature.gastos.FormularioPresupuestoScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<Screen.DetalleVotacion> {
            com.cuentasclaras.app.feature.votaciones.DetalleVotacionScreen(
                onBack = { navController.popBackStack() },
                onEditVotacion = { id -> navController.navigate(Screen.FormularioVotacion(id)) }
            )
        }
        composable<Screen.FormularioVotacion> {
            com.cuentasclaras.app.feature.votaciones.FormularioVotacionScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

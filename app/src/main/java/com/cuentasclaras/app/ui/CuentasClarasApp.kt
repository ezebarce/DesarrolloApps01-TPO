package com.cuentasclaras.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cuentasclaras.app.core.navigation.*

@Composable
fun CuentasClarasApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determinar si mostrar la barra inferior (solo en las 4 pantallas top level)
    val showBottomBar = currentDestination?.hierarchy?.any { dest ->
        TopLevelDestination.entries.any { topLevel ->
            dest.route?.contains(topLevel.route.getRouteName()) == true
        }
    } == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any { 
                            it.route?.contains(destination.route.getRouteName()) == true 
                        } == true
                        
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                // Navegación a destinos Top Level
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { 
                                // TODO: Implementar Badges en el NavigationBarItem
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.text
                                )
                            },
                            label = { Text(destination.text) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        CuentasClarasNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

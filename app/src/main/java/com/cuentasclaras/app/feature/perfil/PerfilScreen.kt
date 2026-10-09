package com.cuentasclaras.app.feature.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentasclaras.app.core.designsystem.components.*
import com.cuentasclaras.app.core.designsystem.theme.Typography
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.core.util.FormateadorMoneda
import com.cuentasclaras.app.feature.inicio.InicioScreenState
import com.cuentasclaras.app.feature.inicio.InicioViewModel
import com.cuentasclaras.app.feature.gastos.DialogoConfirmarEliminacion

@Composable
fun PerfilScreen(
    onBack: () -> Unit,
    viewModel: InicioViewModel = hiltViewModel() // Reutilizamos ViewModel de inicio para el Perfil simple y Reset
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CcTopBarDetail(
                title = "Perfil",
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Cargando -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(state.mensaje)
                }
            }
            is UiState.Vacio -> {}
            is UiState.Contenido -> {
                PerfilContent(
                    state = state.datos,
                    modifier = Modifier.padding(innerPadding),
                    onSolicitarReset = viewModel::solicitarResetDatos,
                    onCancelarReset = viewModel::cancelarResetDatos,
                    onConfirmarReset = {
                        viewModel.confirmarResetDatos()
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
fun PerfilContent(
    state: InicioScreenState,
    modifier: Modifier = Modifier,
    onSolicitarReset: () -> Unit,
    onCancelarReset: () -> Unit,
    onConfirmarReset: () -> Unit
) {
    val propietario = state.propietario

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = propietario?.nombre?.split(" ")?.joinToString("") { it.take(1) } ?: "??",
                style = Typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(propietario?.nombre ?: "", style = Typography.headlineMedium)
        Text("Torre Las Heras 2450", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        CcCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Unidad Funcional", style = Typography.bodyMedium)
                Text(propietario?.unidad ?: "", style = Typography.labelLarge)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Coeficiente (Porcentual)", style = Typography.bodyMedium)
                Text(FormateadorMoneda.formatearPorcentaje(propietario?.coeficiente ?: 0.0), style = Typography.labelLarge)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Rol", style = Typography.bodyMedium)
                if (propietario?.esMiembroConsejo == true) {
                    BadgeAuditoria("Miembro del Consejo", BadgeAuditoriaTipo.VERIFICADO)
                } else {
                    Text("Propietario", style = Typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text("Versión 1.0 (Demo)", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        
        CcDangerOutlinedButton(
            text = "Restablecer datos de ejemplo",
            onClick = onSolicitarReset,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (state.confirmandoReset) {
        DialogoConfirmarEliminacion(
            titulo = "¿Restablecer datos?",
            mensaje = "Se borrarán todos los cambios locales y la base de datos volverá al estado inicial de la demo.",
            onConfirm = onConfirmarReset,
            onDismiss = onCancelarReset
        )
    }
}

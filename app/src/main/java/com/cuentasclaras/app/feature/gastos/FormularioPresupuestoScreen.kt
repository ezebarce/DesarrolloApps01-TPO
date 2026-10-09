package com.cuentasclaras.app.feature.gastos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentasclaras.app.core.designsystem.components.*
import com.cuentasclaras.app.domain.model.EstadoArt

@Composable
fun FormularioPresupuestoScreen(
    onBack: () -> Unit,
    viewModel: FormularioPresupuestoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            onBack()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            CcTopBarDetail(
                title = if (state.presupuestoId == null) "Agregar presupuesto" else "Editar presupuesto",
                onBackClick = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                CcPrimaryButton(
                    text = "Guardar presupuesto",
                    onClick = viewModel::guardar,
                    enabled = !state.isLoading
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CampoTexto(
                value = state.proveedor,
                onValueChange = viewModel::onProveedorChange,
                label = "Proveedor *"
            )

            CampoMonto(
                value = state.montoText,
                onValueChange = viewModel::onMontoChange,
                label = "Monto cotizado *"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampoTexto(
                    value = state.plazoObra,
                    onValueChange = viewModel::onPlazoObraChange,
                    label = "Plazo de obra",
                    modifier = Modifier.weight(1f)
                )

                CampoTexto(
                    value = state.garantiaMesesText,
                    onValueChange = viewModel::onGarantiaChange,
                    label = "Garantía (meses)",
                    modifier = Modifier.weight(1f)
                )
            }

            CampoSelector(
                opciones = EstadoArt.values().map { it.name },
                seleccionado = state.estadoArt.name,
                onSeleccionChange = { viewModel.onEstadoArtChange(EstadoArt.valueOf(it)) },
                label = "Estado ART",
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampoTexto(
                    value = state.calificacionText,
                    onValueChange = viewModel::onCalificacionChange,
                    label = "Calificación (0-5)",
                    modifier = Modifier.weight(1f)
                )

                CampoTexto(
                    value = state.consorciosAuditadosText,
                    onValueChange = viewModel::onConsorciosAuditadosChange,
                    label = "Obras auditadas",
                    modifier = Modifier.weight(1f)
                )
            }

            CampoTexto(
                value = state.etiqueta,
                onValueChange = viewModel::onEtiquetaChange,
                label = "Etiqueta opcional (ej: Express 24hs)"
            )
        }
    }
}

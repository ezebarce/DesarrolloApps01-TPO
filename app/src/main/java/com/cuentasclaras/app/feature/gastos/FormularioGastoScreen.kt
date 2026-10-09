package com.cuentasclaras.app.feature.gastos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentasclaras.app.core.designsystem.components.*
import com.cuentasclaras.app.core.designsystem.theme.Typography
import com.cuentasclaras.app.domain.model.RubroGasto
import com.cuentasclaras.app.domain.model.TipoComprobante
import com.cuentasclaras.app.domain.model.TipoGasto
import com.cuentasclaras.app.domain.model.Urgencia

@Composable
fun FormularioGastoScreen(
    onBack: () -> Unit,
    viewModel: FormularioGastoViewModel = hiltViewModel()
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
                title = if (state.id == null) "Nuevo gasto" else "Editar gasto",
                onBackClick = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                CcPrimaryButton(
                    text = if (state.id == null) "Guardar gasto" else "Guardar cambios",
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
                value = state.titulo,
                onValueChange = viewModel::onTituloChange,
                label = "Título del gasto *"
            )

            CampoTexto(
                value = state.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = "Descripción",
                singleLine = false
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampoSelector(
                    opciones = TipoGasto.values().map { it.name },
                    seleccionado = state.tipo.name,
                    onSeleccionChange = { viewModel.onTipoChange(TipoGasto.valueOf(it)) },
                    label = "Tipo",
                    modifier = Modifier.weight(1f)
                )

                CampoSelector(
                    opciones = RubroGasto.values().map { it.name },
                    seleccionado = state.rubro.name,
                    onSeleccionChange = { viewModel.onRubroChange(RubroGasto.valueOf(it)) },
                    label = "Rubro",
                    modifier = Modifier.weight(1f)
                )
            }

            CampoMonto(
                value = state.montoText,
                onValueChange = viewModel::onMontoChange,
                label = "Monto total *"
            )

            CampoTexto(
                value = state.proveedor,
                onValueChange = viewModel::onProveedorChange,
                label = "Proveedor"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampoSelector(
                    opciones = TipoComprobante.values().map { it.name },
                    seleccionado = state.tipoComprobante.name,
                    onSeleccionChange = { viewModel.onTipoComprobanteChange(TipoComprobante.valueOf(it)) },
                    label = "Comprobante",
                    modifier = Modifier.weight(1f)
                )

                CampoTexto(
                    value = state.numeroComprobante,
                    onValueChange = viewModel::onNumeroComprobanteChange,
                    label = "Nº Comprob. *",
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Licitación", style = Typography.titleMedium)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.requiereLicitacion,
                    onCheckedChange = { viewModel.onRequiereLicitacionChange(it) }
                )
                Text("Requiere 3 presupuestos (Licitación)")
            }

            CampoSelector(
                opciones = Urgencia.values().map { it.name },
                seleccionado = state.urgencia.name,
                onSeleccionChange = { viewModel.onUrgenciaChange(Urgencia.valueOf(it)) },
                label = "Urgencia",
            )
        }
    }
}

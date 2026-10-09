package com.cuentasclaras.app.feature.alertas

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
import com.cuentasclaras.app.domain.model.PrioridadAlerta
import com.cuentasclaras.app.domain.model.TipoAlerta

@Composable
fun FormularioAlertaScreen(
    onBack: () -> Unit,
    viewModel: FormularioAlertaViewModel = hiltViewModel()
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
                title = if (state.id == null) "Nueva alerta" else "Editar alerta",
                onBackClick = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                CcPrimaryButton(
                    text = "Guardar alerta",
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
                label = "Título *"
            )

            CampoTexto(
                value = state.mensaje,
                onValueChange = viewModel::onMensajeChange,
                label = "Mensaje corto *",
                singleLine = false
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampoSelector(
                    opciones = TipoAlerta.values().map { it.name },
                    seleccionado = state.tipo.name,
                    onSeleccionChange = { viewModel.onTipoChange(TipoAlerta.valueOf(it)) },
                    label = "Tipo",
                    modifier = Modifier.weight(1f)
                )

                CampoSelector(
                    opciones = PrioridadAlerta.values().map { it.name },
                    seleccionado = state.prioridad.name,
                    onSeleccionChange = { viewModel.onPrioridadChange(PrioridadAlerta.valueOf(it)) },
                    label = "Prioridad",
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.tipo == TipoAlerta.GASTO || state.tipo == TipoAlerta.VOTACION) {
                CampoTexto(
                    value = state.vinculacionIdTexto,
                    onValueChange = viewModel::onVinculacionIdChange,
                    label = "ID vinculado opcional (Gasto o Votación)"
                )
            }
        }
    }
}

package com.cuentasclaras.app.feature.votaciones

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentasclaras.app.core.designsystem.components.*
import com.cuentasclaras.app.core.designsystem.theme.Typography
import com.cuentasclaras.app.domain.model.TipoAsamblea

@Composable
fun FormularioVotacionScreen(
    onBack: () -> Unit,
    viewModel: FormularioVotacionViewModel = hiltViewModel()
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
            viewModel.updateState { s -> s.copy(error = null) }
        }
    }

    Scaffold(
        topBar = {
            CcTopBarDetail(
                title = if (state.id == null) "Nueva votación" else "Editar votación",
                onBackClick = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                CcPrimaryButton(
                    text = "Guardar votación",
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
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampoSelector(
                    opciones = TipoAsamblea.values().map { it.name },
                    seleccionado = state.tipoAsamblea.name,
                    onSeleccionChange = { v -> viewModel.updateState { it.copy(tipoAsamblea = TipoAsamblea.valueOf(v)) } },
                    label = "Asamblea",
                    modifier = Modifier.weight(1f)
                )

                CampoTexto(
                    value = state.numeroTexto,
                    onValueChange = { v -> viewModel.updateState { it.copy(numeroTexto = v) } },
                    label = "Nº Asamblea *",
                    modifier = Modifier.weight(1f)
                )
            }

            CampoTexto(
                value = state.titulo,
                onValueChange = { v -> viewModel.updateState { it.copy(titulo = v) } },
                label = "Título *"
            )

            CampoTexto(
                value = state.descripcion,
                onValueChange = { v -> viewModel.updateState { it.copy(descripcion = v) } },
                label = "Descripción",
                singleLine = false
            )

            Text("Opciones de la votación", style = Typography.titleMedium)
            
            state.opciones.forEachIndexed { index, op ->
                CcCard {
                    CampoTexto(
                        value = op.titulo,
                        onValueChange = { v ->
                            val nuevas = state.opciones.toMutableList()
                            nuevas[index] = op.copy(titulo = v)
                            viewModel.updateState { it.copy(opciones = nuevas) }
                        },
                        label = "Opción ${index + 1}"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CampoTexto(
                        value = op.descripcion,
                        onValueChange = { v ->
                            val nuevas = state.opciones.toMutableList()
                            nuevas[index] = op.copy(descripcion = v)
                            viewModel.updateState { it.copy(opciones = nuevas) }
                        },
                        label = "Descripción breve"
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = op.recomendada,
                            onCheckedChange = { chk ->
                                val nuevas = state.opciones.toMutableList()
                                // Solo puede haber una recomendada
                                if (chk) {
                                    for (i in nuevas.indices) { nuevas[i] = nuevas[i].copy(recomendada = false) }
                                }
                                nuevas[index] = op.copy(recomendada = chk)
                                viewModel.updateState { it.copy(opciones = nuevas) }
                            }
                        )
                        Text("Recomendada")
                    }
                }
            }

            TextButton(
                onClick = {
                    val nuevas = state.opciones.toMutableList()
                    nuevas.add(OpcionVotoState(titulo = "Nueva Opción"))
                    viewModel.updateState { it.copy(opciones = nuevas) }
                }
            ) {
                Text("Agregar otra opción")
            }
        }
    }
}

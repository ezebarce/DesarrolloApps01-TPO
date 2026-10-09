package com.cuentasclaras.app.feature.gastos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentasclaras.app.core.designsystem.components.*
import com.cuentasclaras.app.domain.model.CategoriaConcepto
import com.cuentasclaras.app.domain.model.EstadoConcepto

@Composable
fun FormularioConceptoScreen(
    onBack: () -> Unit,
    viewModel: FormularioConceptoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }

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
                title = if (state.id == null) "Nuevo impuesto/concepto" else "Editar impuesto/concepto",
                onBackClick = onBack,
                actions = {
                    if (state.id != null) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Eliminar") },
                                onClick = {
                                    showMenu = false
                                    showConfirmDelete = true
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            )

            if (showConfirmDelete) {
                DialogoConfirmarEliminacion(
                    titulo = "¿Eliminar este concepto?",
                    mensaje = "Se borrará permanentemente de los registros.",
                    onConfirm = {
                        showConfirmDelete = false
                        viewModel.eliminar(onEliminado = onBack)
                    },
                    onDismiss = { showConfirmDelete = false }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                CcPrimaryButton(
                    text = if (state.id == null) "Guardar concepto" else "Guardar cambios",
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
                value = state.nombre,
                onValueChange = viewModel::onNombreChange,
                label = "Nombre *"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampoSelector(
                    opciones = CategoriaConcepto.values().map { it.name },
                    seleccionado = state.categoria.name,
                    onSeleccionChange = { viewModel.onCategoriaChange(CategoriaConcepto.valueOf(it)) },
                    label = "Categoría",
                    modifier = Modifier.weight(1f)
                )

                CampoTexto(
                    value = state.organismo,
                    onValueChange = viewModel::onOrganismoChange,
                    label = "Organismo",
                    modifier = Modifier.weight(1f)
                )
            }

            CampoMonto(
                value = state.montoText,
                onValueChange = viewModel::onMontoChange,
                label = "Monto a pagar *"
            )

            CampoSelector(
                opciones = EstadoConcepto.values().map { it.name },
                seleccionado = state.estado.name,
                onSeleccionChange = { viewModel.onEstadoChange(EstadoConcepto.valueOf(it)) },
                label = "Estado de pago",
            )
        }
    }
}

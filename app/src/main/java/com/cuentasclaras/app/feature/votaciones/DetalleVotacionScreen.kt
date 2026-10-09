package com.cuentasclaras.app.feature.votaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentasclaras.app.core.designsystem.components.*
import com.cuentasclaras.app.core.designsystem.theme.CuentasClarasTheme
import com.cuentasclaras.app.core.designsystem.theme.Typography
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.core.util.FormateadorFecha
import com.cuentasclaras.app.core.util.FormateadorMoneda
import com.cuentasclaras.app.domain.model.EstadoVotacion
import com.cuentasclaras.app.domain.model.TipoOpcion
import com.cuentasclaras.app.feature.gastos.DialogoConfirmarEliminacion
import com.cuentasclaras.app.feature.gastos.HojaConfirmacionIdentidad
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun DetalleVotacionScreen(
    onBack: () -> Unit,
    onEditVotacion: (Long) -> Unit,
    viewModel: DetalleVotacionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Contenido) {
            val state = (uiState as UiState.Contenido<DetalleVotacionState>).datos
            state.mostrarMensaje?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.limpiarMensaje()
            }
        }
    }

    Scaffold(
        topBar = {
            var showMenu by remember { mutableStateOf(false) }
            var showConfirmDelete by remember { mutableStateOf(false) }

            CcTopBarDetail(
                title = "Detalle de Votación",
                onBackClick = onBack,
                actions = {
                    if (uiState is UiState.Contenido) {
                        val state = (uiState as UiState.Contenido<DetalleVotacionState>).datos
                        if (state.capacidades.puedeGestionarVotaciones) {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                if (state.votacion.estado != EstadoVotacion.FINALIZADA) {
                                    DropdownMenuItem(
                                        text = { Text("Editar") },
                                        onClick = {
                                            showMenu = false
                                            onEditVotacion(state.votacion.id)
                                        },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                    )
                                }
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
                }
            )

            if (showConfirmDelete) {
                DialogoConfirmarEliminacion(
                    titulo = "¿Eliminar esta votación?",
                    mensaje = "Se borrarán permanentemente sus datos y los votos emitidos.",
                    onConfirm = {
                        showConfirmDelete = false
                        viewModel.eliminarVotacion(onEliminado = onBack)
                    },
                    onDismiss = { showConfirmDelete = false }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            is UiState.Vacio -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(state.mensaje)
                }
            }
            is UiState.Contenido -> {
                DetalleVotacionContent(
                    state = state.datos,
                    modifier = Modifier.padding(innerPadding),
                    onElegirOpcion = viewModel::elegirOpcion,
                    onVotar = viewModel::registrarVoto
                )
            }
        }
    }
}

@Composable
fun DetalleVotacionContent(
    state: DetalleVotacionState,
    modifier: Modifier = Modifier,
    onElegirOpcion: (Long) -> Unit,
    onVotar: () -> Unit
) {
    val v = state.votacion
    var mostrarConfirmacionVoto by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val diasFaltantes = Instant.now().until(v.cierre, ChronoUnit.DAYS)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (v.estado) {
                    EstadoVotacion.ACTIVA -> {
                        BadgeAuditoria(
                            texto = "Cierra en $diasFaltantes días · ${FormateadorFecha.formatearFechaHoraCierre(v.cierre)}",
                            tipo = if (diasFaltantes in 0..3) BadgeAuditoriaTipo.DESVIO else BadgeAuditoriaTipo.NEUTRO
                        )
                    }
                    EstadoVotacion.PROXIMA -> {
                        BadgeAuditoria(
                            texto = "Abre el ${FormateadorFecha.formatearCorta(v.apertura.atZone(ZoneId.systemDefault()).toLocalDate())}",
                            tipo = BadgeAuditoriaTipo.NEUTRO
                        )
                    }
                    EstadoVotacion.FINALIZADA -> {
                        BadgeAuditoria("Asamblea Cerrada", BadgeAuditoriaTipo.NEUTRO)
                    }
                }
                BadgeAuditoria("Asamblea ${v.tipoAsamblea.name.lowercase().replaceFirstChar { it.uppercase() }}", BadgeAuditoriaTipo.NEUTRO)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(v.titulo, style = Typography.headlineMedium)
            if (v.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(v.descripcion, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (state.resultados != null) {
                Spacer(modifier = Modifier.height(16.dp))
                CcCard {
                    val r = state.resultados
                    if (r.hayQuorum) {
                        Text("Quórum legal alcanzado (${FormateadorMoneda.formatearPorcentaje(r.quorumPorcentaje)})", style = Typography.labelMedium, color = CuentasClarasTheme.extendedColors.tertiaryFixed)
                    } else {
                        Text("Esperando quórum", style = Typography.labelMedium, color = CuentasClarasTheme.extendedColors.warning)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    BarraQuorum(porcentajeQuorum = r.quorumPorcentaje, quorumRequerido = v.quorumRequerido)
                }
            }
        }

        if (v.proveedorRecomendado != null && v.montoCentavos != null) {
            item {
                CcCard {
                    Text("Proveedor recomendado: ", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(v.proveedorRecomendado, style = Typography.headlineSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    MontoHeroText(montoCentavos = v.montoCentavos)

                    if (v.rangoMercadoMinCentavos != null && v.rangoMercadoMaxCentavos != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Rango de precio justo", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        BarraRangoMercado(
                            min = v.rangoMercadoMinCentavos,
                            max = v.rangoMercadoMaxCentavos,
                            media = v.montoCentavos,
                            actual = v.montoCentavos
                        )
                    }
                }
            }
        }

        if (state.resultados != null) {
            item {
                CcCard {
                    val r = state.resultados
                    Text("Votación ${r.estadoTexto.lowercase()}", style = Typography.headlineSmall)
                    Text("${r.totalUnidades} de 44 unidades", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val pctAFavor = r.opcionesPonderadas.find { it.opcion.tipo == TipoOpcion.A_FAVOR }?.porcentajeSobreVotos ?: 0.0
                    val pctEnContra = r.opcionesPonderadas.find { it.opcion.tipo == TipoOpcion.EN_CONTRA }?.porcentajeSobreVotos ?: 0.0
                    val pctAbs = r.opcionesPonderadas.find { it.opcion.tipo == TipoOpcion.ABSTENCION }?.porcentajeSobreVotos ?: 0.0
                    
                    BarraSegmentada(pctAFavor, pctEnContra, pctAbs)
                }
            }
        }

        item {
            if (state.miVoto != null) {
                // Ya votó
                val opcionElegida = state.resultados?.opcionesPonderadas?.find { it.opcion.id == state.miVoto.opcionId }?.opcion
                CcCard(modifier = Modifier.background(CuentasClarasTheme.extendedColors.tertiaryFixed.copy(alpha = 0.1f))) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CuentasClarasTheme.extendedColors.tertiaryFixed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ya votaste: ${opcionElegida?.titulo}", style = Typography.headlineSmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Registrado el ${FormateadorFecha.formatearCorta(state.miVoto.fechaHora.atZone(ZoneId.systemDefault()).toLocalDate())}", style = Typography.bodySmall)
                    Text("Comprobante: ${state.miVoto.comprobante}", style = Typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            } else if (v.estado == EstadoVotacion.ACTIVA && state.capacidades.puedeVotar) {
                // Puede votar
                Text("Tu emisión de voto", style = Typography.headlineSmall)
                Text("Ponderación: ${FormateadorMoneda.formatearPorcentaje(state.propietarioActual?.coeficiente ?: 0.0)}", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                state.resultados?.opcionesPonderadas?.forEach { p ->
                    val op = p.opcion
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .padding(16.dp)
                    ) {
                        RadioButton(
                            selected = state.opcionElegidaId == op.id,
                            onClick = { onElegirOpcion(op.id) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(op.titulo, style = Typography.titleMedium)
                                if (op.recomendada) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    BadgeAuditoria("Recomendada", BadgeAuditoriaTipo.VERIFICADO)
                                }
                            }
                            if (op.descripcion.isNotBlank()) {
                                Text(op.descripcion, style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                CcPrimaryButton(
                    text = "Firmar y registrar mi voto",
                    onClick = { mostrarConfirmacionVoto = true },
                    enabled = state.opcionElegidaId != null
                )
                Text("Tu voto es seguro y trazable.", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp).fillMaxWidth())
            }
        }
    }

    if (mostrarConfirmacionVoto) {
        val opcionElegida = state.resultados?.opcionesPonderadas?.find { it.opcion.id == state.opcionElegidaId }?.opcion
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionVoto = false },
            title = { Text("Confirmá tu voto") },
            text = {
                Column {
                    Text("Opción elegida:", style = Typography.labelMedium)
                    Text(opcionElegida?.titulo ?: "", style = Typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Emitiendo como ${state.propietarioActual?.unidad} (${FormateadorMoneda.formatearPorcentaje(state.propietarioActual?.coeficiente ?: 0.0)})", style = Typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacionVoto = false
                    onVotar()
                }) {
                    Text("Confirmar voto")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionVoto = false }) {
                    Text("Cancelar y revisar")
                }
            }
        )
    }

    if (state.confirmandoVoto) {
        HojaConfirmacionIdentidad()
    }
}

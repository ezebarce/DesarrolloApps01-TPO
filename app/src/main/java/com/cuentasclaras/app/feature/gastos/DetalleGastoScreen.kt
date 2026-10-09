package com.cuentasclaras.app.feature.gastos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentasclaras.app.core.designsystem.components.*
import com.cuentasclaras.app.core.designsystem.theme.CuentasClarasTheme
import com.cuentasclaras.app.core.designsystem.theme.Typography
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.core.util.FormateadorFecha
import com.cuentasclaras.app.core.util.FormateadorMoneda
import com.cuentasclaras.app.domain.model.EstadoArt
import com.cuentasclaras.app.domain.usecase.PonderarPresupuestosUseCase

@Composable
fun DetalleGastoScreen(
    onBack: () -> Unit,
    onEditGasto: (Long) -> Unit,
    onAddPresupuesto: (Long) -> Unit,
    onEditPresupuesto: (Long, Long) -> Unit,
    viewModel: DetalleGastoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Contenido) {
            val state = (uiState as UiState.Contenido<DetalleGastoState>).datos
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
                title = "Detalle de gasto",
                onBackClick = onBack,
                actions = {
                    if (uiState is UiState.Contenido) {
                        val state = (uiState as UiState.Contenido<DetalleGastoState>).datos
                        if (state.capacidades.puedeGestionarGastos) {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar") },
                                    onClick = {
                                        showMenu = false
                                        onEditGasto(state.gasto.id)
                                    },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                )
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
                    titulo = "¿Eliminar este gasto?",
                    mensaje = "Se borrarán permanentemente sus datos y los presupuestos asociados.",
                    onConfirm = {
                        showConfirmDelete = false
                        viewModel.eliminarGasto(onEliminado = onBack)
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
                DetalleGastoContent(
                    state = state.datos,
                    modifier = Modifier.padding(innerPadding),
                    onAddPresupuesto = { onAddPresupuesto(state.datos.gasto.id) },
                    onEditPresupuesto = { pId -> onEditPresupuesto(state.datos.gasto.id, pId) },
                    onDeletePresupuesto = viewModel::eliminarPresupuesto,
                    onFirmar = viewModel::firmarPresupuesto
                )
            }
        }
    }
}

@Composable
fun DetalleGastoContent(
    state: DetalleGastoState,
    modifier: Modifier = Modifier,
    onAddPresupuesto: () -> Unit,
    onEditPresupuesto: (Long) -> Unit,
    onDeletePresupuesto: (Long) -> Unit,
    onFirmar: (Long) -> Unit
) {
    val gasto = state.gasto

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (gasto.requiereLicitacion) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BadgeAuditoria(texto = "Votación del Consejo", tipo = BadgeAuditoriaTipo.NEUTRO)
                    BadgeAuditoria(texto = "Quórum: 2/3 votos", tipo = BadgeAuditoriaTipo.NEUTRO)
                }
            }
        }

        item {
            CcCard(modifier = Modifier.fillMaxWidth()) {
                val urgenciaColor = when(gasto.urgencia) {
                    com.cuentasclaras.app.domain.model.Urgencia.ALTA -> MaterialTheme.colorScheme.error
                    com.cuentasclaras.app.domain.model.Urgencia.MEDIA -> CuentasClarasTheme.extendedColors.warning
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Text(
                    text = "URGENCIA ${gasto.urgencia.name}" + if (gasto.requiereLicitacion) " · LICITACIÓN" else "",
                    style = Typography.labelSmall,
                    color = urgenciaColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(gasto.titulo, style = Typography.headlineMedium)
                if (gasto.descripcion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(gasto.descripcion, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    gasto.solicitadoPor?.let { Text("Solicitado por:\n$it", style = Typography.bodySmall) }
                    Text("Fecha:\n${FormateadorFecha.formatearCorta(gasto.fecha)}", style = Typography.bodySmall)
                    gasto.ubicacion?.let { Text("Ubicación:\n$it", style = Typography.bodySmall) }
                }

                if (gasto.rangoMercadoMinCentavos != null && gasto.rangoMercadoMaxCentavos != null && gasto.referenciaMercadoCentavos != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Rango de mercado estimado", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    BarraRangoMercado(
                        min = gasto.rangoMercadoMinCentavos,
                        max = gasto.rangoMercadoMaxCentavos,
                        media = gasto.referenciaMercadoCentavos,
                        actual = gasto.montoCentavos
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(gasto.proveedor, style = Typography.bodyMedium)
                        Text(gasto.numeroComprobante, style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    MontoHeroText(montoCentavos = gasto.montoCentavos)
                }

                Spacer(modifier = Modifier.height(16.dp))
                LineaTiempoTrazabilidad(estadoActual = gasto.estado)
            }
        }

        if (gasto.requiereLicitacion) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${state.presupuestos.size} Presupuestos", style = Typography.headlineSmall)
                    if (state.presupuestos.size >= 3) {
                        BadgeAuditoria("Completos", BadgeAuditoriaTipo.VERIFICADO)
                    } else {
                        BadgeAuditoria("Faltan ${3 - state.presupuestos.size}", BadgeAuditoriaTipo.ADVERTENCIA)
                    }
                }
            }

            items(state.presupuestos) { p ->
                TarjetaPresupuesto(
                    presupuestoPonderado = p,
                    referenciaMercado = gasto.referenciaMercadoCentavos,
                    puedeAprobar = state.capacidades.puedeAprobarPresupuestos,
                    onFirmar = { onFirmar(p.presupuesto.id) },
                    onEdit = { onEditPresupuesto(p.presupuesto.id) },
                    onDelete = { onDeletePresupuesto(p.presupuesto.id) }
                )
            }

            if (state.capacidades.puedeGestionarGastos) {
                item {
                    CcTonalButton(
                        text = "Agregar presupuesto",
                        onClick = onAddPresupuesto
                    )
                }
            }

            item {
                CcCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Votación del Consejo (${state.firmas.size.coerceAtLeast(3)} miembros)", style = Typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    state.firmas.forEach { firma ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                val isVos = firma.propietarioId == state.propietarioActual?.id
                                Text("${firma.miembroNombre} · ${firma.miembroUnidad} ${if(isVos) "(Vos)" else ""}", style = Typography.bodyMedium)
                                if (firma.firmado && firma.presupuestoId != null) {
                                    val proveedor = state.presupuestos.find { it.presupuesto.id == firma.presupuestoId }?.presupuesto?.proveedor ?: ""
                                    Text("Aprobó: $proveedor", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            BadgeAuditoria(
                                texto = if (firma.firmado) "Firmado" else "Por firmar",
                                tipo = if (firma.firmado) BadgeAuditoriaTipo.VERIFICADO else BadgeAuditoriaTipo.NEUTRO
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Nota Legal: Los gastos extraordinarios requieren la justificación con tres presupuestos y aprobación en consejo de administración (Ley 941 CABA).",
                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }

    if (state.confirmandoFirma) {
        HojaConfirmacionIdentidad()
    }
}

@Composable
fun TarjetaPresupuesto(
    presupuestoPonderado: PonderarPresupuestosUseCase.ResultadoPonderacion,
    referenciaMercado: Long?,
    puedeAprobar: Boolean,
    onFirmar: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val p = presupuestoPonderado.presupuesto
    var showMenu by remember { mutableStateOf(false) }

    CcCard(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                if (presupuestoPonderado.esMejorOpcion) {
                    BadgeAuditoria("Mejor opción ponderada", BadgeAuditoriaTipo.VERIFICADO, Modifier.padding(bottom = 8.dp))
                } else if (presupuestoPonderado.esMasEconomica) {
                    BadgeAuditoria("Opción más económica", BadgeAuditoriaTipo.VERIFICADO, Modifier.padding(bottom = 8.dp))
                } else if (p.etiqueta != null) {
                    BadgeAuditoria(p.etiqueta, BadgeAuditoriaTipo.NEUTRO, Modifier.padding(bottom = 8.dp))
                }
                
                Text(p.proveedor, style = Typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = CuentasClarasTheme.extendedColors.warning, modifier = Modifier.size(16.dp))
                    Text(" ${p.calificacion} (${p.consorciosAuditados} consorcios auditados)", style = Typography.bodySmall)
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Editar") }, onClick = { showMenu = false; onEdit() })
                    DropdownMenuItem(text = { Text("Eliminar") }, onClick = { showMenu = false; onDelete() })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            MontoHeroText(montoCentavos = p.montoCentavos)
            if (referenciaMercado != null) {
                val diff = (p.montoCentavos - referenciaMercado).toDouble() / referenciaMercado
                val prefix = if (diff > 0) "+" else ""
                val color = if (diff > 0.10) MaterialTheme.colorScheme.error else CuentasClarasTheme.extendedColors.tertiaryFixed
                Text("$prefix${FormateadorMoneda.formatearPorcentaje(diff)} vs Media", style = Typography.labelSmall, color = color)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column {
                Text("Plazo de obra", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(p.plazoObra, style = Typography.bodyMedium)
            }
            Column {
                Text("Garantía", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${p.garantiaMeses} meses", style = Typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        val artColor = when (p.estadoArt) {
            EstadoArt.AL_DIA -> CuentasClarasTheme.extendedColors.tertiaryFixed
            EstadoArt.EN_TRAMITE -> MaterialTheme.colorScheme.error
            EstadoArt.SIN_ART -> MaterialTheme.colorScheme.error
        }
        val artText = when (p.estadoArt) {
            EstadoArt.AL_DIA -> "ART al día"
            EstadoArt.EN_TRAMITE -> "ART en trámite (Riesgo asegurador)"
            EstadoArt.SIN_ART -> "Sin ART"
        }
        Text(artText, style = Typography.labelMedium, color = artColor)

        if (puedeAprobar && !p.adjudicado) {
            Spacer(modifier = Modifier.height(16.dp))
            if (presupuestoPonderado.esMejorOpcion) {
                CcPrimaryButton(text = "Aprobar este presupuesto", onClick = onFirmar)
            } else {
                CcTonalButton(text = "Seleccionar esta opción", onClick = onFirmar)
            }
        }
    }
}

@Composable
fun DialogoConfirmarEliminacion(
    titulo: String,
    mensaje: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun HojaConfirmacionIdentidad() {
    // Simula el overlay de confirmación
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CcCard {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Validando identidad...", style = Typography.bodyMedium)
        }
    }
}
